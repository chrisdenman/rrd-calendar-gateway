package uk.co.ceilingcat.rrd.gateways.calendaroutputgateway

import arrow.core.Either
import arrow.core.flatMap
import uk.co.ceilingcat.kalendarapi.EventAllday
import uk.co.ceilingcat.kalendarapi.EventDate
import uk.co.ceilingcat.kalendarapi.EventSummary
import uk.co.ceilingcat.kalendarapi.NewEvent
import uk.co.ceilingcat.kalendarapi.createDuration
import uk.co.ceilingcat.kalendarapi.createKalendarApi
import uk.co.ceilingcat.rrd.entities.ServiceDetails
import uk.co.ceilingcat.rrd.usecases.UpcomingOutputGateway
import java.time.Instant
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.io.path.ExperimentalPathApi

/**
 * Construct a new calendar output gateway.
 *
 * @param calendarName the calendar name of which to, add events
 * @param calendarSummaryTemplate the calendar event's summary template
 * @param maximumNotifyDurationSeconds the maximum number of Seconds that we will wait whilst waiting for iCal to finish
 *
 * The `calendarSummaryTemplate` permits the replacement of a single token of the value '<<serviceType>>'; it is
 * replaced with: 'Refuse' when the service type is `ServiceType.REFUSE` and, 'Recycling', when the service is of
 * type `ServiceType.RECYCLING`.
 *
 * @constructor
 */
@ExperimentalPathApi
fun createCalendarOutputGateway(
    calendarName: CalendarName,
    calendarSummaryTemplate: CalendarSummaryTemplate,
    maximumNotifyDurationSeconds: MaximumNotifyDurationSeconds
): UpcomingOutputGateway =
    CalendarOutputGateway(calendarName, calendarSummaryTemplate, maximumNotifyDurationSeconds)

/**
 * @param text the calendar event's summary template
 */
data class CalendarSummaryTemplate(val text: String)

/**
 * @param text the calendar name of which to, add events
 */
data class CalendarName(val text: String)

/**
 * @param magnitude the maximum number of Seconds that we will wait whilst waiting for iCal to finish
 */
data class MaximumNotifyDurationSeconds(val magnitude: Long)

@ExperimentalPathApi
private class CalendarOutputGateway(
    private val calendarName: CalendarName,
    private val calendarSummaryTemplate: CalendarSummaryTemplate,
    private val maximumNotifyDurationSeconds: MaximumNotifyDurationSeconds
) : UpcomingOutputGateway {

    override fun notify(serviceDetails: ServiceDetails): Either<Throwable, Unit> =
        createDuration(maximumNotifyDurationSeconds.magnitude, SECONDS).flatMap { maximumDuration ->
            EventDate(Instant.now().toEpochMilli()).let { eventDate ->
                createKalendarApi(maximumDuration).createEvent(
                    uk.co.ceilingcat.kalendarapi.CalendarName(calendarName.text),
                    NewEvent(
                        summary = EventSummary(makeSubject(serviceDetails)),
                        alldayEvent = EventAllday(true),
                        startDate = eventDate,
                        endDate = eventDate
                    )
                )
            }.map { }
        }

    private fun makeSubject(serviceDetails: ServiceDetails) = replaceTokens(
        listOf(Pair("serviceType", serviceDetails.type.toString().toLowerCase().capitalize())),
        calendarSummaryTemplate.text
    )

    companion object {
        private fun replaceTokens(tokensAndReplacements: List<Pair<String, String>>, templateText: String) =
            tokensAndReplacements.fold(templateText) { acc, (first, second) ->
                acc.replace(
                    "<<$first>>",
                    second
                )
            }
    }
}
