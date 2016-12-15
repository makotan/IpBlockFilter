package com.makotan.lib.web.filter.ipblocker;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by makotan on 2016/12/13.
 */
public class IpBlockerTest {

    @Test
    public void ブロックの挙動を確認() {
        long initTime = 50000L;
        AtomicLong timeMs = new AtomicLong(initTime);
        BlockingStatus status = new BlockingStatus();
        IpBlocker blocker = new IpBlocker(2000L ,10,  status , 3000L , 1000) {
            protected Long currentTimeMs() {
                return timeMs.get();
            }
        };

        for (long i=0; i < 10 ; i++) {
            timeMs.set(initTime + i);
            assertThat(blocker.checkBlock("192.168.0.1").isPresent()).isFalse();
            blocker.calcBlock("192.168.0.1");
        }

        timeMs.set(initTime + 10L);
        assertThat(blocker.checkBlock("192.168.0.1").isPresent()).isTrue();
        blocker.calcBlock("192.168.0.1");

        timeMs.set(initTime + 3011L);
        assertThat(blocker.checkBlock("192.168.0.1").isPresent()).isFalse();
        blocker.calcBlock("192.168.0.1");

    }

    @Test
    public void IPのブロック数の管理を確認() {
        long initTime = 50000L;
        AtomicLong timeMs = new AtomicLong(initTime);
        BlockingStatus status = new BlockingStatus();
        IpBlocker blocker = new IpBlocker(2000L ,10,  status , 3000L , 10) {
            protected Long currentTimeMs() {
                return timeMs.get();
            }
        };

        for (long i=0; i < 10 ; i++) {
            timeMs.set(initTime + i);
            assertThat(blocker.checkBlock("192.168.0.1").isPresent()).isFalse();
            blocker.calcBlock("192.168.0.1");
        }

        for (long i=2; i <= 11 ; i++) {
            timeMs.set(initTime + i);
            assertThat(blocker.checkBlock("192.168.0."+i).isPresent()).isFalse();
            blocker.calcBlock("192.168.0."+i);
        }

        // 時間内なので有効のまま
        timeMs.set(initTime + 10L);
        assertThat(blocker.checkBlock("192.168.0.1").isPresent()).isTrue();
        blocker.calcBlock("192.168.0.1");

        timeMs.set(initTime + 4L);
        assertThat(blocker.checkBlock("192.168.0.15").isPresent()).isFalse();
        blocker.calcBlock("192.168.0.15");

        timeMs.set(initTime + 3011L);
        assertThat(blocker.checkBlock("192.168.0.15").isPresent()).isFalse();
        blocker.calcBlock("192.168.0.15");

        // 時間外なので無効
        timeMs.set(initTime + 3011L);
        assertThat(blocker.checkBlock("192.168.0.1").isPresent()).isFalse();
        blocker.calcBlock("192.168.0.1");

    }

}
