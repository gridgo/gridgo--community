package io.gridgo.bean.test.support;

import java.util.Map;

import com.dslplatform.json.CompiledJson;

import io.gridgo.bean.impl.BReferenceBeautifulPrint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BReferenceBeautifulPrint
@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public class Bar {

	private boolean b;

	private Map<String, Integer> map;
}
