package be.antonbaeckelandt.ssedemo.controllers;

import be.antonbaeckelandt.ssedemo.data.MockOrderGenerator;
import be.antonbaeckelandt.ssedemo.events.NewOrderEvent;
import be.antonbaeckelandt.ssedemo.models.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class OrdersController implements ApplicationListener<NewOrderEvent> {

    @Autowired
    private MockOrderGenerator generator;

    private final Map<SseEmitter, Queue<Order>> clientQueues = new HashMap<>();

    @GetMapping("/orders-sse")
    @CrossOrigin
    public SseEmitter streamOrders() {
        SseEmitter emitter = new SseEmitter();
        clientQueues.put(emitter, new LinkedList<>());

        emitter.onError(ex -> clientQueues.remove(emitter));
        emitter.onCompletion(() -> clientQueues.remove(emitter));
        emitter.onTimeout(() -> clientQueues.remove(emitter));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Queue<Order> queue = clientQueues.get(emitter);
                while (true) {
                    while (!queue.isEmpty()) {
                        Order order = queue.poll();
                        SseEmitter.SseEventBuilder event = SseEmitter.event()
                                .data(order)
                                .name("new-order");
                        emitter.send(event);
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    @Override
    public void onApplicationEvent(NewOrderEvent newOrderEvent) {
        for (SseEmitter emitter : clientQueues.keySet()) {
            Queue<Order> queue = clientQueues.get(emitter);
            queue.add(newOrderEvent.getOrder());
        }
    }
}
