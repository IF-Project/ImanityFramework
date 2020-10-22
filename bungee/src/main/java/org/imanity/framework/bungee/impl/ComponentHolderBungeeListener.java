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

package org.imanity.framework.bungee.impl;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.imanity.framework.bungee.Imanity;
import org.imanity.framework.bungee.plugin.annotation.PluginProvider;
import org.imanity.framework.bungee.util.PluginUtil;
import org.imanity.framework.plugin.component.ComponentHolder;

public class ComponentHolderBungeeListener extends ComponentHolder {
    @Override
    public Class<?>[] type() {
        return new Class[] {Listener.class};
    }

    @Override
    public Object newInstance(Class<?> type) {
        Plugin plugin = Imanity.PLUGIN;
        Class<? extends Plugin> pluginClass = Plugin.class;

        PluginProvider provider = type.getAnnotation(PluginProvider.class);
        if (provider != null) {
            pluginClass = provider.value();
            plugin = PluginUtil.getPlugin(pluginClass);
        }

        Listener listener;

        constructor: {

            try {

                listener = (Listener) type
                        .getConstructor(pluginClass)
                        .newInstance(plugin);
                break constructor;

            } catch (ReflectiveOperationException ex) {
            }

            try {
                listener = (Listener) type.newInstance();

                break constructor;
            } catch (ReflectiveOperationException ex) {

            }

            throw new RuntimeException("Couldn't find valid constructor for " + type.getSimpleName());
        }

        Imanity.getProxy().getPluginManager().registerListener(plugin, listener);
        return listener;
    }
}
