package org.leicheng.bingwallpaper.bean;

import java.io.Serializable;

public class BingImage implements Serializable {
	private static final long serialVersionUID = 831496905284787841L;
	private Long startdate;
	private Long fullstartdate;
	private Long enddate;
	private String url;
	private String urlBase;
	private String copyright;
	private String copyrightlink;
	private Integer drk;
	private Integer top;
	private Integer bot;
//	private org.w3c.dom.Node xml;
	@Override
	public String toString() {
		return "BingImage [startdate=" + startdate + ", fullstartdate=" + fullstartdate + ", enddate=" + enddate + ", url=" + url + ", urlBase=" + urlBase + ", copyrightlink=" + copyrightlink
				+ ", drk=" + drk + ", top=" + top + ", bot=" + bot + "]";
	}

	public Long getStartdate() {
		return startdate;
	}

	public void setStartdate(Long startdate) {
		this.startdate = startdate;
	}

	public Long getFullstartdate() {
		return fullstartdate;
	}

	public void setFullstartdate(Long fullstartdate) {
		this.fullstartdate = fullstartdate;
	}

	public Long getEnddate() {
		return enddate;
	}

	public void setEnddate(Long enddate) {
		this.enddate = enddate;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlBase() {
		return urlBase;
	}

	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}

	public String getCopyrightlink() {
		return copyrightlink;
	}

	public void setCopyrightlink(String copyrightlink) {
		this.copyrightlink = copyrightlink;
	}

	public Integer getDrk() {
		return drk;
	}

	public void setDrk(Integer drk) {
		this.drk = drk;
	}

	public Integer getTop() {
		return top;
	}

	public void setTop(Integer top) {
		this.top = top;
	}

	public Integer getBot() {
		return bot;
	}

	public void setBot(Integer bot) {
		this.bot = bot;
	}

//	public org.w3c.dom.Node getXml() {
//		return xml;
//	}
//
//	public void setXml(org.w3c.dom.Node xml) {
//		this.xml = xml;
//	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

}
