package com.exapmle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class Main {
    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        Queue<Integer> minHeap, maxHeap;
        minHeap = new PriorityQueue<>();
        maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        BigDecimal sum = new BigDecimal(0);
        Map<String, Long> timeCarrier = new HashMap();
        int count = 0;
        TicketList ticketList = objectMapper.readValue(readResourcesFile("/tickets.json"), TicketList.class);
        for (Ticket ticket : ticketList.getTickets()) {
            if ((ticket.getOriginName().equals("Владивосток") && ticket.getDestinationName().equals("Тель-Авив"))
                    || (ticket.getOriginName().equals("Тель-Авив") && ticket.getDestinationName().equals("Владивосток"))) {
                Long minutesBetweenDate = calculateMinutesBetweenDepartureAndArrivalDatetime(ticket.getDepartureDate(), ticket.getDepartureTime(), ticket.getArrivalDate(), ticket.getArrivalTime());
                if (timeCarrier.containsKey(ticket.getCarrier())) {
                    if (timeCarrier.get(ticket.getCarrier()) > minutesBetweenDate) {
                        timeCarrier.put(ticket.getCarrier(), minutesBetweenDate);
                    }
                } else {
                    timeCarrier.put(ticket.getCarrier(), minutesBetweenDate);
                }
                BigDecimal temp = sum;
                sum = temp.add(ticket.getPrice());
                addPriceToHeap(ticket.getPrice().intValue(), minHeap, maxHeap);
                count++;
            }
        }
        double answer = getMedian(minHeap, maxHeap);
        System.out.println("Медианное значение цен - " + answer);
        System.out.println("Среднее значение цен - " + sum.divide(new BigDecimal(count)).intValue());
        System.out.println("Минимальная время полёта для каждого перевозчика - " + timeCarrier);
    }

    private static Long calculateMinutesBetweenDepartureAndArrivalDatetime(LocalDate departureDate, LocalTime departureTime, LocalDate arrivalDate, LocalTime arrivalTime) {
        LocalDateTime localDepartureDateTime = departureDate.atTime(departureTime);
        LocalDateTime localArrivalDateTime = arrivalDate.atTime(arrivalTime);
        long minutesBetweenDate = ChronoUnit.MINUTES.between(localDepartureDateTime, localArrivalDateTime);
        return minutesBetweenDate;
    }

    private static void addPriceToHeap(Integer price, Queue<Integer> minHeap, Queue<Integer> maxHeap) {
        if (!minHeap.isEmpty() && price < minHeap.peek()) {
            maxHeap.offer(price);
            if (maxHeap.size() > minHeap.size() + 1) {
                minHeap.offer(maxHeap.poll());
            }
        } else {
            minHeap.offer(price);
            if (minHeap.size() > maxHeap.size() + 1) {
                maxHeap.offer(minHeap.poll());
            }
        }
    }

    private static Double getMedian(Queue<Integer> minHeap, Queue<Integer> maxHeap) {
        double median;
        if (minHeap.size() > maxHeap.size()) {
            median = minHeap.peek();
        } else {
            median = (minHeap.peek() + maxHeap.peek()) / 2;
        }
        return median;
    }

    public static InputStream readResourcesFile(String fileName) {
        InputStream inputStream = Main.class.getResourceAsStream(fileName);
        return inputStream;
    }

}
