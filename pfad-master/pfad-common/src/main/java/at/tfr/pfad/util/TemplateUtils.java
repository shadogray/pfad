package at.tfr.pfad.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;


public class TemplateUtils implements Serializable {

    private PropertyUtilsBean pub = new PropertyUtilsBean();

	public String replace(String template, Map<String, Object> map) {
		return replace(template, map, null);
	}

	public String replace(String template, Map<String, Object> map, String defaultValue) {
		return replace(template, map.entrySet(), defaultValue);
	}

	public String replace(String template, Collection<Entry<String, Object>> map, String defaultValue) {
		String val = replace(template, map);
		if (StringUtils.isBlank(val))
			return defaultValue;
		return val;
	}

	public String replace(String template, Collection<Entry<String, Object>> map) {
		Map<String, Object> vals = new HashMap<>();
		map.forEach(e -> vals.put(e.getKey(), e.getValue()));
		StrSubstitutor strSub = new PositiveStrSubstitutor(new BeansStrLookup(map)).withValues(vals);
		strSub.setEnableSubstitutionInVariables(true);
		return strSub.replace(template);
	}

	public class BeansStrLookup extends StrLookup<String> {

		private Collection<Entry<String, Object>> map;

		public BeansStrLookup(Collection<Entry<String, Object>> map) {
			this.map = map;
		}

		@Override
		public String lookup(final String key) {
			Optional<Entry<String, Object>> opt = map.stream().filter(e -> key.toLowerCase().startsWith(e.getKey().toLowerCase())).findFirst();
			if (opt.isPresent()) {
				if (opt.get().getKey().equalsIgnoreCase(key)) {
					return "" + opt.get().getValue();
				}
				if (key.toLowerCase().startsWith(opt.get().getKey().toLowerCase() + ".")) {
					try {
						return "" + pub.getProperty(opt.get().getValue(), key.substring(opt.get().getKey().length() + 1));
					} catch (Exception e) {}
				}
			}
			return "";
		}
	}

}
