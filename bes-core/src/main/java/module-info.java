module com.github.jochenw.bes.core {
	exports com.github.jochenw.bes.core.api;
	exports com.github.jochenw.bes.core.model;

	requires com.github.jochenw.afw.core;
	requires com.github.jochenw.afw.di;
	requires jakarta.inject;
}