/**
 * Codesnippet Javadoc Doclet
 * Copyright (C) 2015-2018 Jaroslav Tulach - jaroslav.tulach@apidesign.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.0 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-3.0.
 */
package org.apidesign.javadoc.testing;

import reactor.core.publisher.Flux;

public class UseFlux {

    /**
     * The main entry point for running the application.
     *
     * @param args Array of arguments to the main method.
     */
    public static void main(String[] args) {
        Flux<Integer> flux = getFlux();
    }

    /**
     * Returns a {@link Flux} of integers.
     *
     * @return A flux of integers.
     */
    public static Flux<Integer> getFlux() {
        return Flux.just(1, 2, 3, 4);
    }

}
