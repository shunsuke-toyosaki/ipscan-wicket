package com.example.ipscan.util;

import com.example.ipscan.data.IPInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

public class IPScan {
    public static void main(String[] args) {
        var ipscan = new IPScan("192.168.11.0");
        var networkIps = ipscan.scanAll(255);
        System.out.println("Devices connected to the network:");
        networkIps.forEach(System.out::println);
    }

    /**
     * @param firstIpInTheNetwork e.g: 192.168.1.0
     * @param numOfIps            e.g: 254
     * @return
     */

    private final String firstIpInTheNetwork;

    public IPScan(String firstIpInTheNetwork) {
        this.firstIpInTheNetwork = firstIpInTheNetwork;
    }

    public List<IPInfo> scanAll(int numOfIps) {
        ForkJoinPool pool = new ForkJoinPool(100);
        var host = firstIpInTheNetwork.substring(0, firstIpInTheNetwork.length() - 1);
        try {
            return pool.submit(() -> IntStream
                    .range(0, numOfIps)
                    .parallel()
                    .mapToObj(
                            v -> host + v
                    )
                    .map(this::scan)
                    .filter(Objects::nonNull)
                    //                .sorted()
                    .toList()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public IPInfo scan(String ipAddress) {
        try {
            var inetAddress = InetAddress.getByName(ipAddress);
            try {
                return new IPInfo(inetAddress, inetAddress.isReachable(500));
            } catch (IOException e) {
                e.printStackTrace();
                return new IPInfo(inetAddress, false);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }
}