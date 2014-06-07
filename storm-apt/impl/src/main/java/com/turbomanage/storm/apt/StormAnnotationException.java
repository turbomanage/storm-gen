package com.turbomanage.storm.apt;

import javax.lang.model.element.Element;

public class StormAnnotationException extends RuntimeException {

	private static final long serialVersionUID = -4916405018264078766L;
	private Element element;

	public StormAnnotationException(String msg, Element e) {
		super(msg);
		this.element = e;
	}

	public Element getElement() {
		return element;
	}

}
