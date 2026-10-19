package com.yurii.pavlenko.myassistant.flights.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ReportQuotaTest {

    @Test
    public void withoutLimitNothingIsFullOrRunningOut() {
        ReportQuota quota = new ReportQuota(ReportQuota.NO_LIMIT, 5000);
        assertFalse(quota.isFull());
        assertFalse(quota.isRunningOut());
    }

    @Test
    public void farFromLimitThereIsNoWarning() {
        ReportQuota quota = new ReportQuota(100, 89);
        assertEquals(11, quota.remaining());
        assertFalse(quota.isRunningOut());
        assertFalse(quota.isFull());
    }

    @Test
    public void warnsWhenTenPlacesAreLeft() {
        ReportQuota quota = new ReportQuota(100, 90);
        assertEquals(10, quota.remaining());
        assertTrue(quota.isRunningOut());
        assertFalse(quota.isFull());
    }

    @Test
    public void lastPlaceStillWarns() {
        ReportQuota quota = new ReportQuota(100, 99);
        assertEquals(1, quota.remaining());
        assertTrue(quota.isRunningOut());
    }

    @Test
    public void atTheLimitItIsFullAndNoLongerJustWarning() {
        ReportQuota quota = new ReportQuota(100, 100);
        assertTrue(quota.isFull());
        assertFalse(quota.isRunningOut());
        assertEquals(0, quota.remaining());
    }

    @Test
    public void loweringTheLimitBelowTheCountMakesItFull() {
        ReportQuota quota = new ReportQuota(50, 80);
        assertTrue(quota.isFull());
        assertEquals(0, quota.remaining());
    }

    @Test
    public void smallLimitWarnsFromTheStart() {
        assertTrue(new ReportQuota(5, 0).isRunningOut());
    }

    @Test
    public void sanitizeKeepsTheLimitInRange() {
        assertEquals(0, ReportQuota.sanitize(-3));
        assertEquals(100, ReportQuota.sanitize(100));
        assertEquals(ReportQuota.MAX_LIMIT, ReportQuota.sanitize(1_000_000));
    }
}
