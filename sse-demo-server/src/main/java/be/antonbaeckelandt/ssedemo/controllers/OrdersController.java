package be.antonbaeckelandt.ssedemo.controllers;

import be.antonbaeckelandt.ssedemo.data.MockOrderGenerator;
import be.antonbaeckelandt.ssedemo.events.NewOrderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class OrdersController implements ApplicationListener<NewOrderEvent> {

    private static final Logger LOGGER = Logger.getLogger(OrdersController.class.getSimpleName());

    private static final int HEART_BEAT_INTERVAL = 5000;

    @Autowired
    private MockOrderGenerator generator;

    private final List<SseEmitter> emitters = new LinkedList<>();

    public OrdersController() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            while (true) {
                try {
                    sendHeartbeats();
                    Thread.sleep(HEART_BEAT_INTERVAL);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage());
                }
            }
        });
    }

    @GetMapping("/orders-sse")
    @CrossOrigin
    public SseEmitter streamOrders() {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);

        emitter.onError(ex -> emitters.remove(emitter));
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }

    @Override
    public void onApplicationEvent(NewOrderEvent newOrderEvent) {
        sendSseToClients("new-order", newOrderEvent.getOrder());
    }

    private void sendSseToClients(String name, Object message) {
        for (SseEmitter emitter : emitters) {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .data(message)
                        .name(name);
                emitter.send(event);
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    private void sendHeartbeats() {
        for (SseEmitter emitter : emitters) {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .data("heartbeat")
                        .name("heartbeat");
                emitter.send(event);
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }
    }

}
