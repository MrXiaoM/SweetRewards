package top.mrxiaom.sweet.rewards.utils;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import top.mrxiaom.pluginbase.api.IRunTask;
import top.mrxiaom.pluginbase.api.IScheduler;
import top.mrxiaom.sweet.rewards.SweetRewards;
import top.mrxiaom.sweet.rewards.func.AbstractPluginHolder;

public class FoliaScheduler implements IScheduler {
    private final FoliaLib foliaLib;
    public FoliaScheduler(SweetRewards plugin) {
        foliaLib = new FoliaLib(plugin);
    }

    public Task wrap(WrappedTask task) {
        return new Task(task);
    }

    @Override
    public IRunTask runTask(Runnable runnable) {
        foliaLib.getScheduler().runNextTick((t) -> runnable.run());
        return DummyTask.INSTANCE;
    }

    @Override
    public IRunTask runTaskLater(Runnable runnable, long delay) {
        return wrap(foliaLib.getScheduler().runLater(runnable, delay));
    }

    @Override
    public IRunTask runTaskTimer(Runnable runnable, long delay, long period) {
        return wrap(foliaLib.getScheduler().runTimer(runnable, delay, period));
    }

    @Override
    public IRunTask runTaskAsync(Runnable runnable) {
        foliaLib.getScheduler().runNextTick((t) -> runnable.run());
        return DummyTask.INSTANCE;
    }

    @Override
    public IRunTask runTaskLaterAsync(Runnable runnable, long delay) {
        return wrap(foliaLib.getScheduler().runLaterAsync(runnable, delay));
    }

    @Override
    public IRunTask runTaskTimerAsync(Runnable runnable, long delay, long period) {
        return wrap(foliaLib.getScheduler().runTimerAsync(runnable, delay, period));
    }

    @Override
    public void cancelTasks() {
        foliaLib.getScheduler().cancelAllTasks();
    }

    public static class Task implements IRunTask {
        private final WrappedTask task;
        public Task(WrappedTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }
    }

    public static class DummyTask implements IRunTask {
        public static final DummyTask INSTANCE = new DummyTask();
        private DummyTask() {}
        @Override
        public void cancel() {
        }
    }
}
