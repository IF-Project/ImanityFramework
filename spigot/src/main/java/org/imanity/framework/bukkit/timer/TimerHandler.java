/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.bukkit.timer;

import com.google.common.collect.ImmutableList;
import org.bukkit.scheduler.BukkitTask;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.timer.event.TimerStartEvent;
import org.imanity.framework.bukkit.util.TaskUtil;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service(name = "timer")
public class TimerHandler implements Runnable, IService {

    private List<Timer> timers;

    private BukkitTask task;

    public void init() {
        this.timers = new ArrayList<>();
        this.task = TaskUtil.runRepeated(this, 5L);
    }

    @Override
    public void stop() {
        this.task.cancel();
    }

    public void add(Timer timer) {
        TimerStartEvent event = new TimerStartEvent(timer);
        Imanity.callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        this.timers.add(timer);
        timer.start();
    }

    public synchronized void clear(Timer timer) {
        this.timers.remove(timer);
    }

    public synchronized void clear(Class<? extends Timer> timerClass) {
        this.timers.removeIf(timerClass::isInstance);
    }

    public boolean isTimerRunning(Class<? extends Timer> timerClass) {
        return this.getTimer(timerClass) != null;
    }

    public synchronized <T extends Timer> T getTimer(Class<T> timerClass) {
        return (T) this.timers
                .stream()
                .filter(timerClass::isInstance)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void run() {

        synchronized (this.timers) {
            Iterator<Timer> iterator = this.timers.iterator();

            while (iterator.hasNext()) {
                Timer timer = iterator.next();
                if (!timer.isPaused()) {

                    timer.tick();
                    if (timer.isTimerElapsed() && timer.finish()) {
                        timer.clear(false);
                        iterator.remove();
                    }
                }
            }
        }
    }
}
