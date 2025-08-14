package raubach.frickl.next.pojo;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@ToString
public class YearCounts
{
	private Integer year;
	private Integer count;
}
