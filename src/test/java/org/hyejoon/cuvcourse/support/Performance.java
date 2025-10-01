package org.hyejoon.cuvcourse.support;

import java.util.Arrays;

public final class Performance {

    private Performance() {
    }

    public static long[] samplesMs(int warmup, int runs, Runnable r) {
        for (int i = 0; i < warmup; i++) {
            r.run();
        }
        long[] arr = new long[runs];
        for (int i = 0; i < runs; i++) {
            long t0 = System.nanoTime();
            r.run();
            arr[i] = (System.nanoTime() - t0) / 1_000_000;
        }
        return arr;
    }

    public static String toMarkdown(String nameA, long[] a, String nameB, long[] b, String nameC,
        long[] c) {
        int n = Math.max(a.length, b.length);
        StringBuilder sb = new StringBuilder();
        sb.append("| # | ").append(nameA).append(" (ms) | ").append(nameB).append(" (ms) | ")
            .append(nameC).append(" (ms) |\n");
        sb.append("|---|----|----|----|\n");
        for (int i = 0; i < n; i++) {
            String va = i < a.length ? Long.toString(a[i]) : "";
            String vb = i < b.length ? Long.toString(b[i]) : "";
            String vc = i < c.length ? Long.toString(c[i]) : "";
            sb.append("| ").append(i + 1).append(" | ").append(va).append(" | ").append(vb)
                .append(" | ").append(vc)
                .append(" |\n");
        }
        var saStats = stats(a);
        var sbStats = stats(b);
        var scStats = stats(c);
        sb.append("\n> ")
            .append(nameA).append(" avg=").append(saStats.avg)
            .append(" ms, ")
            .append(nameB).append(" avg=").append(sbStats.avg)
            .append(" ms, ")
            .append(nameC).append(" avg=").append(scStats.avg)
            .append(" ms\n");
        return sb.toString();
    }

    private static Stats stats(long[] ms) {
        long avg = Arrays.stream(ms).sum() / Math.max(1, ms.length);
        return new Stats(avg);
    }

    private record Stats(long avg) {

    }
}
