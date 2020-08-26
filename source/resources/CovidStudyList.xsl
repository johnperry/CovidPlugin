<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="yes" />

<xsl:variable name="url">
	<xsl:text>/qic/</xsl:text>
	<xsl:value-of select="/Patient/@name"/>
</xsl:variable>

<xsl:template match="/Patient">
	<html>
		<head>
			<title>QIC - Study List</title>
			<link rel="Stylesheet" type="text/css" media="all" href="/qicstyles.css"></link>
			<style>
				table {margin:0px; padding:0px;}
				td {padding:0px; vertical-align:top; font-family:sans-serif; padding-right:10px;}
			</style>
		</head>
		<body>
		<a href="/qic"><h2>Open-QIC</h2></a>
		<center>
			<h1>QIC Study List for <xsl:value-of select="@name"/></h1>
			<p>
				<table class="studylist">
					<tr>
						<th>Study</th>
						<th>Series</th>
						<th>Image</th>
						<th colspan="2">View</th>
					</tr>
					<xsl:apply-templates select="Study">
						<xsl:sort select="@name"/>
					</xsl:apply-templates>
				</table>
			</p>
		</center>
		</body>
	</html>
</xsl:template>
			
<xsl:template match="Study">
	<tr>
		<td>
			<xsl:value-of select="@name"/>
		</td>
	</tr>
	<xsl:apply-templates select="Series">
		<xsl:sort select="@name"/>
	</xsl:apply-templates>
</xsl:template>

<xsl:template match="Series">
	<tr>
		<td/>
		<td>
			<xsl:value-of select="@name"/>
		</td>
	</tr>
	<xsl:apply-templates select="Image">
		<xsl:sort select="@name"/>
	</xsl:apply-templates>
</xsl:template>

<xsl:template match="Image">
	<tr>
		<td/>
		<td/>
		<td>
			<xsl:value-of select="@name"/>
		</td>
		<td class="center">
			<input type="button" onclick="window.open('{$url}/elements?path={@path}','elements')" value="Elements"/>
		</td>
		<td class="center">
			<input type="button" onclick="window.open('{$url}/image?path={@path}','image')" value="Image"/>
		</td>
	</tr>
</xsl:template>

</xsl:stylesheet>
