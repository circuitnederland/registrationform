import org.cyclos.entities.users.SystemRecord
import org.cyclos.model.utils.FileInfo
import org.cyclos.server.utils.SerializableInputStream

import java.nio.charset.StandardCharsets

def recordId = formParameters.recordId
def record = entityManagerHandler.find(SystemRecord.class, scriptHelper.unmaskId(recordId))
def fields = scriptHelper.wrap(record)
def batchId = fields.batchId
def xml = fields.xml
def bytes = xml.getBytes(StandardCharsets.UTF_8)

return new FileInfo(
    name: "PAIN_008_${batchId}.xml",
    contentType: "application/octet-stream",
    length: bytes.length,
    content: new SerializableInputStream(bytes)
)
