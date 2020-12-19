package uk.co.ceilingcat.rrd.gateways.calendaroutputgateway

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.io.path.ExperimentalPathApi

@TestInstance(PER_CLASS)
internal class CalendarOutputGatewayTests {

    @ExperimentalPathApi
    @Test
    fun `That we can create gateway instances with createCalendarOutputGateway()`() {
        val maximumNotifyDurationSeconds = MaximumNotifyDurationSeconds(30L)
        val calendarName = CalendarName("TEST CALENDAR NAME")
        val calendarSummaryTemplate = CalendarSummaryTemplate("<<token>> today")
        Assertions.assertNotNull(
            createCalendarOutputGateway(calendarName, calendarSummaryTemplate, maximumNotifyDurationSeconds)
        )
    }
}
