package org.eventrails.demo.api.query;

import org.eventrails.common.modeling.messaging.query.Multiple;
import org.eventrails.demo.api.view.DemoRichView;
import org.eventrails.common.modeling.messaging.payload.Query;

public class DemoRichViewFindAllQuery extends Query<Multiple<DemoRichView>> {
	private Integer limit;
	private Integer offset;





	public DemoRichViewFindAllQuery(int limit, int offset) {
		this.limit = limit;
		this.offset = offset;
	}

	public DemoRichViewFindAllQuery() {
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
}
