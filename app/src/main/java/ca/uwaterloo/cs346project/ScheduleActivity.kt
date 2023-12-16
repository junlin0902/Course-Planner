package ca.uwaterloo.cs346project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ca.uwaterloo.cs346project.ui.theme.Cs346projectTheme
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt


@Serializer(forClass = LocalDateTime::class)
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        val string = value.format(formatter)
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val string = decoder.decodeString()
        return LocalDateTime.parse(string, formatter)
    }
}

@Serializable
data class Event(
    val name: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    var start: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    var end: LocalDateTime,
    val description: String? = null,
)

val EventTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
@Composable
fun BasicEvent(
    event: Event,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(end = 2.dp, bottom = 2.dp)
            .background(Color(0xFFe6be8a), shape = RoundedCornerShape(4.dp))
            .padding(4.dp)
    ) {
        Text(
            text = "${event.start.format(EventTimeFormatter)} - ${event.end.format(EventTimeFormatter)}",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Black
        )

        Text(
            text = event.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        if (event.description != null) {
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
        }
    }
}

private var Events = listOf<Event>(
    Event(
        name = "placeholder",
        start = LocalDateTime.parse("2023-05-14T00:00:00"),
        end = LocalDateTime.parse("2023-05-14T00:00:00"),
        description = "keep the calendar open to 7 days",
    ),
    Event(
        name = "placeholder2",
        start = LocalDateTime.parse("2023-05-20T00:00:00"),
        end = LocalDateTime.parse("2023-05-20T00:00:00"),
        description = "keep the calendar open to 7 days",
    ),
//    Event(
//        name = "CS 121",
//        color = Color(0xFF1B998B),
//        start = LocalDateTime.parse("2023-05-14T16:50:00"),
//        end = LocalDateTime.parse("2023-05-14T18:00:00"),
//        description = "An arbitrary CS course placeholder description.",
//    ),
//    Event(
//        name = "CS 122",
//        color = Color(0xFFF4BFDB),
//        start = LocalDateTime.parse("2023-05-15T09:30:00"),
//        end = LocalDateTime.parse("2023-05-15T11:00:00"),
//        description = "An arbitrary CS course placeholder description.",
//    ),
//    Event(
//        name = "CS 135",
//        color = Color(0xFF6DD3CE),
//        start = LocalDateTime.parse("2023-05-16T11:00:00"),
//        end = LocalDateTime.parse("2023-05-16T12:15:00"),
//        description = "An arbitrary CS course placeholder description.",
//    ),
//    Event(
//        name = "CS 136",
//        color = Color(0xFF1B998B),
//        start = LocalDateTime.parse("2023-05-20T12:00:00"),
//        end = LocalDateTime.parse("2023-05-20T13:50:00"),
//        description = "An arbitrary CS course placeholder description.",
//    ),
)
@Composable
fun createEventList (
    currentUser: String,
) : List<Event>? {
    var allEnrollments by remember { mutableStateOf<List<Event>?>(null) }
    val dbHelper = UserDBHelper()
    dbHelper.getAllEnrollments(currentUser) { enrollments, error ->
        if (error != null) {
            println("Error: ${error.message}")
        } else {
            allEnrollments = enrollments
        }
    }
    return allEnrollments
}

class ScheduleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            Cs346projectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Cs346projectTheme {
                        val currUser = intent.getStringExtra("CURRENT_USER") ?: ""
                        val sampleEvents = createEventList(currUser)
                        if (sampleEvents != null) {
                            Schedule(sampleEvents)
                        }
                        else {
                            Schedule(listOf())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BasicSchedule(
    events: List<Event>,
    modifier: Modifier = Modifier,
    eventContent: @Composable (event: Event) -> Unit = { BasicEvent(event = it) },
    minDate: LocalDate = events.minByOrNull(Event::start)!!.start.toLocalDate(),
    maxDate: LocalDate = minDate.plus(6, ChronoUnit.DAYS),
    dayWidth: Dp,
    hourHeight: Dp,
) {
    val numDays = ChronoUnit.DAYS.between(minDate, maxDate).toInt() + 1
    Layout(
        content = {
            events.sortedBy(Event::start).forEach { event ->
                Box(modifier = Modifier.eventData(event)) {
                    eventContent(event)
                }
            }
        },
        modifier = modifier,
    ) { measureables, constraints ->
        val height = hourHeight.roundToPx() * 24
        val width = dayWidth.roundToPx() * numDays
        val placeablesWithEvents = measureables.map { measureable ->
            val event = measureable.parentData as Event
            val eventDurationMinutes = ChronoUnit.MINUTES.between(event.start, event.end)
            val eventHeight = ((eventDurationMinutes / 60f) * hourHeight.toPx()).roundToInt()
            val placeable = measureable.measure(constraints.copy(minWidth = dayWidth.roundToPx(), maxWidth = dayWidth.roundToPx(), minHeight = eventHeight, maxHeight = eventHeight))
            Pair(placeable, event)
        }
        layout(width, height) {
            placeablesWithEvents.forEach { (placeable, event) ->
                val eventOffsetMinutes = ChronoUnit.MINUTES.between(LocalTime.MIN, event.start.toLocalTime())
                val eventY = ((eventOffsetMinutes / 60f) * hourHeight.toPx()).roundToInt()
                val eventOffsetDays = ChronoUnit.DAYS.between(minDate, event.start.toLocalDate()).toInt()
                val eventX = eventOffsetDays * dayWidth.roundToPx()
                placeable.place(eventX, eventY)
            }
        }
    }
}

private class EventDataModifier(
    val event: Event,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = event
}

private fun Modifier.eventData(event: Event) = this.then(EventDataModifier(event))

@Composable
fun BasicDayHeader(
    day: LocalDate,
    modifier: Modifier = Modifier,
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)
    Text(
        text = day.format(formatter),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    )
}

@Composable
fun ScheduleHeader(
    minDate: LocalDate,
    maxDate: LocalDate,
    dayWidth: Dp,
    modifier: Modifier = Modifier,
    dayHeader: @Composable (day: LocalDate) -> Unit = { BasicDayHeader(day = it) },
) {
    Row(modifier = modifier) {
        val numDays = 7
        //ChronoUnit.DAYS.between(minDate, maxDate).toInt() + 1
        repeat(numDays) { i ->
            Box(modifier = Modifier.width(dayWidth)) {
                dayHeader(minDate.plusDays(i.toLong()))
            }
        }
    }
}

@Composable
fun Schedule(
    events: List<Event>,
    modifier: Modifier = Modifier,
    eventContent: @Composable (event: Event) -> Unit = { BasicEvent(event = it) },
    minDate: LocalDate = LocalDateTime.parse("2023-05-14T00:00:00").toLocalDate(),
    maxDate: LocalDate = minDate.plusDays(6),
) {
    val dayWidth = 256.dp
    val hourHeight = 64.dp
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    var sidebarWidth by remember { mutableStateOf(0) }
    Column(modifier = modifier) {
        ScheduleHeader(
            minDate = minDate,
            maxDate = maxDate,
            dayWidth = dayWidth,
            modifier = Modifier
                .padding(start = with(LocalDensity.current) { sidebarWidth.toDp() })
                .horizontalScroll(horizontalScrollState)
        )
        Row(modifier = Modifier.weight(1f)) {
            ScheduleSidebar(
                hourHeight = hourHeight,
                modifier = Modifier
                    .verticalScroll(verticalScrollState)
                    .onGloballyPositioned { sidebarWidth = it.size.width }
            )
            BasicSchedule(
                events = events,
                eventContent = eventContent,
                minDate = minDate,
                maxDate = maxDate,
                dayWidth = dayWidth,
                hourHeight = hourHeight,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(verticalScrollState)
                    .horizontalScroll(horizontalScrollState)
            )
        }
    }
}

private val HourFormatter = DateTimeFormatter.ofPattern("h a")

@Composable
fun BasicSidebarLabel(
    time: LocalTime,
    modifier: Modifier = Modifier,
) {
    Text(
        text = time.format(HourFormatter),
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
    )
}

@Composable
fun ScheduleSidebar(
    hourHeight: Dp,
    modifier: Modifier = Modifier,
    label: @Composable (time: LocalTime) -> Unit = { BasicSidebarLabel(time = it) },
) {
    Column(modifier = modifier) {
        val startTime = LocalTime.MIN
        repeat(24) { i ->
            Box(modifier = Modifier.height(hourHeight)) {
                label(startTime.plusHours(i.toLong()))
            }
        }
    }
}