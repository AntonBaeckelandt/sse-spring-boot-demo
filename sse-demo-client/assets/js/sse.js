'use strict';

const SERVER_URL = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', init);

function init() {
    // Use this if the SSE stream is located on another origin:
    // const evtSource = new EventSource(SERVER_URL + '/orders-sse', {withCredentials: true});

    // When hosted on the same origin, "withCredentials" is not necessary
    const evtSource = new EventSource(SERVER_URL + '/orders-sse');
    evtSource.addEventListener("new-order", processNewOrderEvent);
}

function processNewOrderEvent(event) {
    const orderContainer = document.getElementById('order-list');
    const order = JSON.parse(event.data);
    orderContainer.innerHTML += `<li>${order.customerName}: ${order.mealName}</li>`;
}