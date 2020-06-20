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
				td {padding-left:20px; padding-right:20px; font-family:monospace; font-size:18pt; text-align:left;}
			</style>
		</head>
		<body>
		<center>
			<h1>QIC Site List</h1>
			<p>
			<table border="0">
				<tr>
					<th>Site ID</th>
					<th>Email</th>
					<th>Site</th>
					<th>User</th>
				</tr>
				<xsl:for-each select="Site">
					<tr>
						<td><xsl:value-of select="@id"/></td>
						<td><xsl:value-of select="@email"/></td>
						<td><xsl:value-of select="@site"/></td>
						<td><xsl:value-of select="@user"/></td>
					</tr>
				</xsl:for-each>
			</table>
			</p>
		</center>
		</body>
	</html>
</xsl:template>

</xsl:stylesheet>
