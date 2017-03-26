package test.barinek.continuum.allocations

import com.fasterxml.jackson.core.type.TypeReference
import io.barinek.continuum.allocations.AllocationInfo
import io.barinek.continuum.allocations.ProjectInfo
import io.barinek.continuum.restsupport.get
import io.barinek.continuum.restsupport.post
import io.barinek.continuum.testsupport.TestApp
import io.barinek.continuum.testsupport.TestControllerSupport
import io.barinek.continuum.testsupport.TestScenarioSupport
import io.barinek.continuum.testsupport.TestSpringApp
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class AllocationControllerTest : TestControllerSupport() {
    val app = TestSpringApp(8081, "io.barinek.continuum.allocations",
            "io.barinek.continuum.jdbcsupport",
            "io.barinek.continuum.restsupport",
            "io.barinek.continuum.testsupport")

    @Before
    fun setUp() {
        app.start()
    }

    @After
    fun tearDown() {
        app.stop()
    }

    @Test
    fun testCreate() {
        TestScenarioSupport().loadTestScenario("jacks-test-scenario")

        val uServiceProjects = TestApp(8883) { mapper, outputStream ->
            mapper.writeValue(outputStream, ProjectInfo(true))
        }
        uServiceProjects.start()

        val json = "{\"projectId\":55432,\"userId\":4765,\"firstDay\":\"2014-05-16\",\"lastDay\":\"2014-05-26\"}"
        val response = template.post("http://localhost:8081/allocations", json)
        val actual = mapper.readValue(response, AllocationInfo::class.java)

        assert(actual.id > 0)
        assertEquals(55432L, actual.projectId)
        assertEquals(4765L, actual.userId)
        assertEquals(LocalDate.of(2014, 5, 16), actual.firstDay)
        assertEquals(LocalDate.of(2014, 5, 26), actual.lastDay)
        assertEquals("allocation info", actual.info)

        uServiceProjects.stop()
    }

    @Test
    fun testFailedCreate() {
        TestScenarioSupport().loadTestScenario("jacks-test-scenario")

        val uServiceProjects = TestApp(8883) { mapper, outputStream ->
            mapper.writeValue(outputStream, ProjectInfo(false))
        }
        uServiceProjects.start()

        val json = "{\"projectId\":55432,\"userId\":4765,\"firstDay\":\"2014-05-16\",\"lastDay\":\"2014-05-26\"}"
        val response = template.post("http://localhost:8081/allocations", json)
        assert(response.isBlank())

        uServiceProjects.stop()
    }

    @Test
    fun testFind() {
        TestScenarioSupport().loadTestScenario("jacks-test-scenario")

        val response = template.get("http://localhost:8081/allocations?projectId={projectId}", Pair("projectId", "55432"))
        val list: List<AllocationInfo> = mapper.readValue(response, object : TypeReference<List<AllocationInfo>>() {})
        val actual = list.first()

        assertEquals(754L, actual.id)
        assertEquals(55432L, actual.projectId)
        assertEquals(4765L, actual.userId)
        assertEquals(LocalDate.of(2015, 5, 17), actual.firstDay)
        assertEquals(LocalDate.of(2015, 5, 18), actual.lastDay)
        assertEquals("allocation info", actual.info)
    }
}