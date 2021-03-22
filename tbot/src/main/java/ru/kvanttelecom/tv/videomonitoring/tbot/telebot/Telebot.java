package ru.kvanttelecom.tv.videomonitoring.tbot.telebot;

import com.google.common.base.Splitter;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreamworkerln.spring.utils.common.threadpool.BlockingJobPool;
import ru.dreamworkerln.spring.utils.common.threadpool.JobResult;
import ru.kvanttelecom.tv.videomonitoring.tbot.configurations.properties.ApplicationProperties;
import ru.kvanttelecom.tv.videomonitoring.tbot.entities.CameraStatusMap;
import ru.kvanttelecom.tv.videomonitoring.utils.data.Camera;
import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.springframework.util.StringUtils.*;
import static ru.dreamworkerln.spring.utils.common.StringUtils.isBlank;
import static ru.kvanttelecom.tv.videomonitoring.utils.dto.constants.Constants.PROTOCOL;

@Service
@Slf4j
public class Telebot {

    private static final int TELEGRAM_MAX_MESSAGE_LENGTH = 4000;
    private static final Duration TELEGRAM_SEND_TIMEOUT_MIN = Duration.ofSeconds(2);
    private static final Duration TELEGRAM_SEND_TIMEOUT_MAX = Duration.ofSeconds(32);
    private final AtomicReference<Duration> telegramSendTimeout = new AtomicReference<>(TELEGRAM_SEND_TIMEOUT_MIN);

    // messages handlers
    private final ConcurrentMap<String, BiConsumer<Long, String>> handlers = new ConcurrentHashMap<>();

    private TelegramBot bot;

    //private static final Splitter TELEGRAM_LENGTH_SPLITTER = Splitter.fixedLength(TELEGRAM_MAX_MESSAGE_LENGTH);


    // Non-negative AtomicInteger incrementator
    //private final LongUnaryOperator idIncrementator = (i) -> i == Long.MAX_VALUE ? 0 : i + 1;
    // connection id generator
    //private final AtomicLong idGen =  new AtomicLong();
    //private final ConcurrentMap<Long, SendMessage> messageQueue = new ConcurrentHashMap<>();

    private final BlockingJobPool<SendMessage,SendResponse> jobPool =
        new BlockingJobPool<>(5, TELEGRAM_SEND_TIMEOUT_MIN, null);


    @Autowired
    ApplicationProperties props;

    @Autowired
    private CameraStatusMap cameras;

    @PostConstruct
    private void postConstruct() {
        log.info("Staring telegram bot");

        handlers.put("/help", this::help);
        handlers.put("/echo", this::echo);
        handlers.put("/ping", this::ping);
        handlers.put("/cameras", this::cameras);

        bot = new TelegramBot.Builder(props.getBotToken()).build();

        bot.setUpdatesListener(updates -> {

            // process updates
            for (Update update : updates) {

                try {

                    Long chatId = update.message().chat().id();
                    String text = update.message().text();

                    if (isBlank(text)) {
                        continue;
                    }

                    String[] commandArray = text.split("[ @]");
                    if (commandArray.length > 0) {
                        String command = commandArray[0];

                        if (handlers.containsKey(command)) {
                            String body = trimWhitespace(text.substring(command.length()));

                            handlers.get(command).accept(chatId, body);
                        }
                    }

                }
                catch (Exception e) {
                    log.error("bot receive/handle message error: ", e);
                }
            }

            // return id of last processed update or confirm them all
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    /**
     * Send message to telegram
     * @param chatId whom
     * @param message text
     * @return SendResponse
     */
    @SneakyThrows
    public SendResponse sendMessage(Long chatId, String message) {

        if(isBlank(message)) {
            return null;
        }

        SendResponse result = null;

        // разбивка по страницам 4k
        for (final String chunk : stringSplitter(message)) {

            SendMessage msg = new SendMessage(chatId, chunk);

            Throwable exception;
            do {

                // отправка
                JobResult<SendMessage,SendResponse> jobResult = jobPool.execTimeout(msg,
                    a -> new JobResult<>(msg, bot.execute(msg)), telegramSendTimeout.get());

                exception = jobResult.getException();
                result = jobResult.getResult();
                //log.trace("Telebot send result: {}", result);;

                // need to wait on error
                if(exception != null) {

                    log.trace("Telebot send exception:", exception);

                    // дополнительно ждем
                    Thread.sleep(telegramSendTimeout.get().toMillis());
                    // increase timeout
                    calculateNewDuration(+1);
                }
                else {
                    // decrease timeout
                    calculateNewDuration(-1);
                }
            }
            while (exception != null);
        }

        return result;
    }

    private void calculateNewDuration(int direction) {

        telegramSendTimeout.getAndUpdate(d -> {

            Duration result = d;
            if (direction == 1) {
                result = d.multipliedBy(2);
            }
            if (direction == -1) {
                result = d.dividedBy(2);
            }

            if (result.toSeconds() > TELEGRAM_SEND_TIMEOUT_MAX.toSeconds() ||
                result.toSeconds() < TELEGRAM_SEND_TIMEOUT_MIN.toSeconds()) {
                result = d;
            }
            return result;
        });
    }

    private void help(Long chatId, String text) {

        String message ="\n" +
            "/cameras - list of not working cameras" +
            "/help - this help" +
            "\n" + "/echo [text] - echo [text]" +
            "\n" + "/ping - echo-reply" +
            "\n" + "Cameras info: " + PROTOCOL + props.getAddress() + "/cameras" + "\n";

        SendResponse response = sendMessage(chatId, message);
    }

    private void echo(Long chatId, String text) {
        SendResponse response = sendMessage(chatId, text);

    }

    private void ping(Long chatId, String text) {
        SendResponse response = sendMessage(chatId, "pong");
    }


    /**
     * Show DOWN cameras
     */
    private void cameras(Long chatId, String text) {


        StringBuilder sb = new StringBuilder("DOWN CAMERAS:\n\n");
        List<String> lines = new ArrayList<>();

        for (Map.Entry<String, Camera> entry : cameras.entrySet()) {

            Camera camera = entry.getValue();

            if(camera.isAlive()) {
                continue;
            }

            String title = camera.getTitle();
            if(isBlank(title)) {
                title = camera.getName();
            }
            lines.add(title + "\n");
        }
        lines.sort(Comparator.comparing(Function.identity(), String.CASE_INSENSITIVE_ORDER));


        lines.forEach(sb::append);

        if(lines.size() == 0) {
            sb.setLength(0);
            sb.append("ALL ONLINE");
        }

        SendResponse response = sendMessage(chatId, sb.toString());
    }


    private List<String> stringSplitter(String text) {

        if(text.charAt(text.length() - 1) != '\n') {
            text = text.concat("\n");
        }

        List<String> result = new ArrayList<>();
        int length = Integer.MAX_VALUE;


        while ((length = Math.min(text.length(), TELEGRAM_MAX_MESSAGE_LENGTH)) > 0) {

            while (length > 0 && text.charAt(length - 1) != '\n') {
                length--;
            }
            result.add(text.substring(0, length));
            text = text.substring(length);
        }

        return result;
    }


}
