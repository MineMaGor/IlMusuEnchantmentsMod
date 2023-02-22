package com.ilmusu.ilmusuenchantments.mixins.interfaces;

import java.util.function.Consumer;

public interface _IPlayerTickers
{
    void addTicker(Ticker ticker);

    class Ticker
    {
        protected final int duration;
        protected int remainingTime;

        protected Consumer<Ticker> onEnter = (ticker) -> {};
        protected Consumer<Ticker> onTick  = (ticker) -> {};
        protected Consumer<Ticker> onExit  = (ticker) -> {};

        public Ticker(int duration)
        {
            this.duration = duration;
            this.remainingTime = duration;
        }

        public Ticker onEntering(Consumer<Ticker> action)
        {
            this.onEnter = action;
            return this;
        }

        public Ticker onTicking(Consumer<Ticker> action)
        {
            this.onTick = action;
            return this;
        }

        public Ticker onExiting(Consumer<Ticker> action)
        {
            this.onExit = action;
            return this;
        }

        public void start()
        {
            // The ticker started, execute the runnable
            this.onEnter.accept(this);
        }

        public void tick()
        {
            // Decreasing the remaining time
            if(this.hasFinished())
                return;
            --this.remainingTime;

            // The ticker is ticking, execute the runnable
            this.onTick.accept(this);

            // The ticker finished executing, execute the runnable
            if(this.remainingTime == 0)
                this.onExit.accept(this);
        }

        public void setFinished()
        {
            this.remainingTime = 0;
        }

        public boolean hasFinished()
        {
            return this.remainingTime <= 0;
        }
    }
}
