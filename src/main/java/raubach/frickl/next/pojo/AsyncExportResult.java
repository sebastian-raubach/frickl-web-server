package raubach.frickl.next.pojo;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@ToString
public class AsyncExportResult
{
	private String userToken;
	private String token;
	private ExportStatus status;
	private String jobId;
	private Date createdOn;
}
