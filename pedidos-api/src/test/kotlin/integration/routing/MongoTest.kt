package integration.routing

import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

class MongoTest {

    @JvmField
    val mongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:latest")).apply {
        withExposedPorts(27017)
        start()
    }
}