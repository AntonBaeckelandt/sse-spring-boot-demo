package be.antonbaeckelandt.ssedemo.data;

import be.antonbaeckelandt.ssedemo.events.NewOrderEvent;
import be.antonbaeckelandt.ssedemo.models.Order;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MockOrderGenerator {

    private static int lastGeneratedId = 0;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @PostConstruct
    public void init() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            while (true) {
                try {
                    publishNewOrderEvent();
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void publishNewOrderEvent() {
        Faker faker = new Faker();
        Order order = new Order(++lastGeneratedId, faker.name().fullName(), faker.food().ingredient());
        NewOrderEvent customSpringEvent = new NewOrderEvent(this, order);
        applicationEventPublisher.publishEvent(customSpringEvent);
    }

}