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

package org.imanity.framework.redis.message;

import org.imanity.framework.plugin.component.ComponentHolder;
import org.imanity.framework.plugin.component.ComponentRegistry;
import org.imanity.framework.plugin.service.Autowired;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;
import org.imanity.framework.redis.RedisService;
import org.imanity.framework.redis.message.annotation.HandleMessage;
import org.imanity.framework.redis.subscription.RedisPubSub;
import org.imanity.framework.util.AccessUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service(name = "messageService", dependencies = {"serverHandler"})
public class MessageService implements IService {

    private RedisPubSub<Object> redisPubSub;
    private String channel;

    private Map<Class<?>, List<MessageListenerData>> messageListeners;

    @Autowired
    private RedisService redisService;

    @Override
    public void preInit() {
        this.messageListeners = new ConcurrentHashMap<>(12);

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {

            @Override
            public Object newInstance(Class<?> type) {
                Object instance = super.newInstance(type);
                registerListener((MessageListener) instance);

                return instance;
            }

            @Override
            public Class<?>[] type() {
                return new Class[] {MessageListener.class};
            }

        });
    }

    @Override
    public void init() {
        this.channel = "imanity-server";

        this.redisPubSub = new RedisPubSub<>(this.channel, this.redisService, Object.class);
        this.redisPubSub.subscribe((message -> {
            List<MessageListenerData> listeners = this.messageListeners.get(message.getClass());
            if (listeners == null) {
                return;
            }
            for (MessageListenerData data : listeners) {
                try {
                    data.getMethod().invoke(data.getInstance(), message);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }));
    }

    public void sendMessage(Object message) {
        try {
            if (message == null) {
                throw new IllegalStateException("The Message given a null serialized data!");
            }

            this.redisPubSub.publish(message);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void registerListener(MessageListener messageListener) {
        Method[] methods = messageListener.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.getDeclaredAnnotation(HandleMessage.class) == null) {
                continue;
            }
            if (method.getParameters().length != 1) {
                continue;
            }
            Class<?> messageClass = method.getParameterTypes()[0];

            List<MessageListenerData> listeners;
            if (this.messageListeners.containsKey(messageClass)) {
                listeners = this.messageListeners.get(messageClass);
            } else {
                listeners = new ArrayList<>();
                this.messageListeners.put(messageClass, listeners);
            }

            try {
                AccessUtil.setAccessible(method);
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException(e);
            }
            listeners.add(new MessageListenerData(messageListener, method, messageClass));
        }
    }
}
