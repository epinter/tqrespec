/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
 */

/*    This file is part of TQ Respec.

    TQ Respec is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TQ Respec is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TQ Respec.  If not, see <http://www.gnu.org/licenses/>.
*/

package br.com.pinter.tqrespec;

import br.com.pinter.tqrespec.util.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionTest {
    private Version version;

    @BeforeEach
    void setUp() {
        version = new Version("1.1.0");
    }

    @Test
    void Given_smallerVersionNumber_Then_returnNegative() {
        assertEquals(-1, new Version("000001.00000.22222").compareTo(version));
        assertEquals(-1, new Version("000000.5.999").compareTo(version));
        assertEquals(-1, new Version("1.0.0.0.0.0.1").compareTo(version));
        assertEquals(-1, new Version("1.0.0").compareTo(version));
        assertEquals(-1, new Version("1.0").compareTo(version));
        assertEquals(-1, new Version("0.1").compareTo(version));
    }

    @Test
    void Given_greaterVersionNumber_Then_returnPositive() {
        assertEquals(1, new Version("1.1.0.0.0.1").compareTo(version));
        assertEquals(1, new Version("000001.0001.00001").compareTo(version));
        assertEquals(1, new Version("000002.00005.999").compareTo(version));
        assertEquals(1, new Version("2000.1.1.0").compareTo(version));
        assertEquals(1, new Version("2.0").compareTo(version));
        assertEquals(1, new Version("2.0.0").compareTo(version));
        assertEquals(1, new Version("1.2.0").compareTo(version));
    }

    @Test
    void Given_equalVersionNumber_Then_returnZero() {
        assertEquals(0, new Version("1.1.0.0.0.0.0").compareTo(version));
        assertEquals(0, new Version("00001.0000001.00000").compareTo(version));
        assertEquals(0, new Version("1.1").compareTo(version));
    }

}