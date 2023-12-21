package io.kyligence.benchmark.utils;

public class ProgressBarUtil {


    public static void printProgressBar(String message, int progress, long current, long total) {
        System.out.print("\r" + message + "  Progress: [");
        int numChars = progress / 2;

        for (int i = 0; i < 50; i++) {
            if (i < numChars) {
                System.out.print("=");
            } else if (i == numChars) {
                System.out.print(">");
            } else {
                System.out.print(" ");
            }
        }

        System.out.print("] " + progress + "%  " + current + "/" + total);
    }

    private static void simulateTaskExecution() {
        // 模拟任务执行的延迟
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int totalSteps = 100;

        for (int currentStep = 0; currentStep <= totalSteps; currentStep++) {
            int progress = (currentStep * 100) / totalSteps;
            printProgressBar("test", progress, currentStep, totalSteps);
            // 模拟任务执行
            simulateTaskExecution();
        }
    }
}
