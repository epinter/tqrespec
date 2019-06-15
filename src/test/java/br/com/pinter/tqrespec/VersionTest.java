/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VersionTest {
    private Version version;

    @Before
    public void setUp() throws Exception {
        version = new Version("1.1.0");
    }

    @Test
    public void Given_smallerVersionNumber_Then_returnNegative() {
        Assert.assertEquals(new Version("000001.00000.22222").compareTo(version),-1);
        Assert.assertEquals(new Version("000000.5.999").compareTo(version),-1);
        Assert.assertEquals(new Version("1.0.0.0.0.0.1").compareTo(version),-1);
        Assert.assertEquals(new Version("1.0.0").compareTo(version),-1);
        Assert.assertEquals(new Version("1.0").compareTo(version),-1);
        Assert.assertEquals(new Version("0.1").compareTo(version),-1);
    }

    @Test
    public void Given_greaterVersionNumber_Then_returnPositive() {
        Assert.assertEquals(new Version("1.1.0.0.0.1").compareTo(version),1);
        Assert.assertEquals(new Version("000001.0001.00001").compareTo(version), 1);
        Assert.assertEquals(new Version("000002.00005.999").compareTo(version), 1);
        Assert.assertEquals(new Version("2000.1.1.0").compareTo(version), 1);
        Assert.assertEquals(new Version("2.0").compareTo(version),1);
        Assert.assertEquals(new Version("2.0.0").compareTo(version),1);
        Assert.assertEquals(new Version("1.2.0").compareTo(version),1);
    }

    @Test
    public void Given_equalVersionNumber_Then_returnZero() {
        Assert.assertEquals(new Version("1.1.0.0.0.0.0").compareTo(version),0);
        Assert.assertEquals(new Version("00001.0000001.00000").compareTo(version),0);
        Assert.assertEquals(new Version("1.1").compareTo(version),0);
    }

}