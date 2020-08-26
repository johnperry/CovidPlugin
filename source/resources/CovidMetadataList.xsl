<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="yes" />

<xsl:variable name="url">
	<xsl:text>/qic/</xsl:text>
	<xsl:value-of select="/Patient/@name"/>
</xsl:variable>

<xsl:template match="/Metadata">
	<html>
		<head>
			<title>QIC - Metadata List</title>
			<link rel="Stylesheet" type="text/css" media="all" href="/qicstyles.css"></link>
			<style>
				table {margin:0px; padding:0px;}
				td {font-family:sans-serif; text-align:left;}
				td.value {font-family:sans-serif; text-align:right;}
				td.units {font-family:sans-serif; text-align:left;}
				td.big {padding:3px; padding-left:0; font-family:sans-serif; font-weight:bold; font-size:18pt;}
			</style>
		</head>
		<body>
		<a href="/qic"><h2>Open-QIC</h2></a>
		<center>
			<h1>QIC Metadata List</h1>
			<p>
				<table border="0">
					<xsl:apply-templates/>
				</table>
			</p>
		</center>
		</body>
	</html>
</xsl:template>
			
<xsl:template match="Patient">
	<tr><td class="big">Patient</td></tr>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="Symptoms">
	<tr><td class="big">Symptoms</td></tr>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="LabValues">
	<tr><td class="big">LabValues</td></tr>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="ImagingProcedure">
	<tr><td class="big">ImagingProcedure</td></tr>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="Treatment">
	<tr><td class="big">Treatment</td></tr>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="Outcome">
	<tr><td class="big">Outcome</td></tr>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="*">
	<tr>
		<td><xsl:value-of select="local-name()"/></td>
		<td class="value"><xsl:value-of select="."/></td>
		<xsl:if test="@units">
			<td class="units"><xsl:value-of select="@units"/></td>
		</xsl:if>
	</tr>
</xsl:template>

</xsl:stylesheet>
