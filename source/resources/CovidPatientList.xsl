<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="yes" />

<xsl:param name="admin"/>

<xsl:template match="/Patients">
	<html>
		<head>
			<title>QIC - Search Patients</title>
			<link rel="Stylesheet" type="text/css" media="all" href="/BaseStyles.css"></link>
			<style>
				td {padding:5px;}
			</style>
		</head>
		<body>
		<center>
			<h1>QIC Patient List</h1>
			<p>
			<table border="0">
				<xsl:for-each select="Patient">
					<xsl:variable name="url">
						<xsl:text>/qic/</xsl:text>
						<xsl:value-of select="PatientID"/>
					</xsl:variable>
					<tr>
						<td>
							<xsl:value-of select="PatientID"/>
						</td>
						<td>
							<input type="button" onclick="window.open('{$url}','_self')" value="Metadata"/>
						</td>
						<td>
							<input type="button" onclick="window.open('{$url}/export','_self')" value="Download"/>
						</td>
						<td>
							<input type="button" onclick="window.open('{$url}/list','_self')" value="List Images"/>
						</td>
						<xsl:if test="$admin='yes'">
							<td>
								<input type="button" onclick=
										"if (confirm('Are you sure?')) window.open('{$url}/delete','_self');"
									value="Delete"/>
							</td>
						</xsl:if>
					</tr>
				</xsl:for-each>
			</table>
			</p>
		</center>
		</body>
	</html>
</xsl:template>

</xsl:stylesheet>
