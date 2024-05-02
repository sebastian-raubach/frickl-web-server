package raubach.frickl.next.pojo;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@ToString
public class AsyncImageExportResult extends AsyncExportResult
{
	private Integer imageCount;
}
