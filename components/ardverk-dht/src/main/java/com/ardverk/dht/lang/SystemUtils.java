/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ardverk.dht.lang;

public class SystemUtils {
    
    private static long TIME = 0;
    
    private SystemUtils() {}
    
    /**
     * Returns the {@link System#currentTimeMillis()} with one little twist.
     * It makes sure that time is always progressing forward.
     * 
     * long a = System.currentTimeMillis();
     * long b = System.currentTimeMillis();
     * assert (b >= a);
     * 
     * @see System#currentTimeMillis()
     */
    public static synchronized long currentTimeMillis() {
        return (TIME = Math.max(TIME, System.currentTimeMillis()));
    }
    
    /**
     * Returns the {@link System#nanoTime()}.
     * 
     * @see System#nanoTime()
     */
    public static long nanoTime() {
        return System.nanoTime();
    }
}
