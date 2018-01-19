package at.tfr.pfad.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrLookup;
import org.apache.commons.text.StrSubstitutor;

public class TemplateUtils implements Serializable {

	private PropertyUtilsBean pub = new PropertyUtilsBean();

	public String replace(String template, Map<String,Object> map, String defaultValue) {
		String val = replace(template, map);
		if (StringUtils.isBlank(val))
			return defaultValue;
		return val;
	}

	public String replace(String template, Map<String,Object> map) {
		StrSubstitutor strSub = new StrSubstitutor(new BeansStrLookup(map));
		return strSub.replace(template);
	}
	
	public class BeansStrLookup extends StrLookup<String> {

		private Map<String,Object> map;
		
		public BeansStrLookup(Map<String,Object> map) {
			this.map = map;
		}
		
		@Override
		public String lookup(final String key) {
			Optional<Entry<String, Object>> opt = map.entrySet().stream().filter(e -> key.startsWith(e.getKey())).findFirst();
			if (opt.isPresent()) {
				if (opt.get().getKey().equals(key)) {
					return ""+opt.get().getValue();
				}
				if (key.startsWith(opt.get().getKey()+".")){
					try {	
						return ""+pub.getProperty(opt.get().getValue(), key.substring(opt.get().getKey().length()+1));
					} catch (Exception e) {}
				}
			}
			return "";
		}
	}
	
}
