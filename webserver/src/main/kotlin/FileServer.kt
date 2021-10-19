import ru.sber.filesystem.VFilesystem
import ru.sber.filesystem.VPath
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
class FileServer {

    private val OK_200_RESPONSE =
        "HTTP/1.0 200 OK\r\n" +
                "Server: FileServer\r\n" +
                "\r\n"
    private val NOT_FOUND_404_RESPONSE =
        "HTTP/1.0 404 Not Found\r\n" +
                "Server: FileServer\r\n" +
                "\r\n"

    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs     A proxy filesystem to serve files from. See the VFilesystem
     *               class for more detailed documentation of its usage.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    @Throws(IOException::class)
    fun run(socket: ServerSocket, fs: VFilesystem) {
        socket.use {
            while (!socket.isClosed) {
                process(socket.accept(), fs)
            }
        }
    }

    private fun process(client: Socket, fs: VFilesystem) {
        client.use {
            val br = client.getInputStream().bufferedReader()
            val bw = client.getOutputStream().bufferedWriter()
            val req = br.readLine().split(" ")
            val reqType = req[0].uppercase()
            val reqPath = req[1]

            if (reqType == "GET" && fs.readFile(VPath(reqPath)) != null) {
                bw.write(OK_200_RESPONSE + fs.readFile(VPath(reqPath)))
                bw.flush()
            } else if (reqType == "GET" && fs.readFile(VPath(reqPath)) == null) {
                bw.write(NOT_FOUND_404_RESPONSE)
                bw.flush()
            }
        }
    }
}