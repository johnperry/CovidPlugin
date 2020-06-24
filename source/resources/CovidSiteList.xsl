<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="yes" />

<xsl:template match="/SiteIndex">
	<html>
		<head>
			<title>QIC - Site List</title>
			<link rel="Stylesheet" type="text/css" media="all" href="/BaseStyles.css"></link>
			<style>
				th {padding-left:20px; padding-right:20px; font-family:sans-serif; font-size:18pt; text-align:left;}
				td {padding-left:20px; padding-right:20px; font-family:monospace; font-size:18pt; text-align:left; vertical-align:top;}
			</style>
		</head>
		<body>
		<center>
			<h1>QIC Site List</h1>
			<p>
			<table border="1">
				<tr>
					<th>Site ID</th>
					<th>Site</th>
					<th>User</th>
				</tr>
				<xsl:for-each select="Site">
					<tr>
						<td><xsl:value-of select="@id"/></td>
						<td>
							<xsl:value-of select="@site"/>
							<xsl:if test="@adrs1">
								<br/>
								<xsl:value-of select="@adrs1"/>
							</xsl:if>
							<xsl:if test="@adrs2">
								<br/>
								<xsl:value-of select="@adrs2"/>
							</xsl:if>
							<xsl:if test="@adrs3">
								<br/>
								<xsl:value-of select="@adrs3"/>
							</xsl:if>
						</td>
						<td>
							<xsl:value-of select="@user"/>
							<xsl:if test="@email">
								<br/>
								<xsl:value-of select="@email"/>
							</xsl:if>
							<xsl:if test="@phone">
								<br/>
								<xsl:value-of select="@phone"/>
							</xsl:if>
						</td>
					</tr>
				</xsl:for-each>
			</table>
			</p>
		</center>
		</body>
	</html>
</xsl:template>

</xsl:stylesheet>
