<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="yes" />

<xsl:param name="admin"/>

<xsl:template match="/Patients">
	<html>
		<head>
			<title>QIC - Search Patients</title>
			<link rel="Stylesheet" type="text/css" media="all" href="/qicstyles.css"></link>
		</head>
		<body>
		<a href="/"><h2>Open-QIC</h2></a>
		<center>
			<h1>QIC Patient List</h1>
			<p>
			<table class="ptlist">
				<tr>
					<th>Open-QIC ID</th>
					<th>COVID +</th>
					<th>Age</th>
					<th>Gender</th>
					<th>Outcome</th>
					<th>View</th>
					<th>Download</th>
					<th>List</th>
					<xsl:if test="$admin='yes'">
						<th>Admin</th>
					</xsl:if>
				</tr>
				<xsl:for-each select="Patient">
					<xsl:variable name="url">
						<xsl:text>/qic/</xsl:text>
						<xsl:value-of select="PatientID"/>
					</xsl:variable>
					<tr>
						<td>
							<xsl:value-of select="PatientID"/>
						</td>
						<td class="center">
							<xsl:value-of select="CovidPlus"/>
						</td>
						<td class="center">
							<xsl:value-of select="PatientAge"/>
						</td>
						<td class="center">
							<xsl:value-of select="PatientSex"/>
						</td>
						<td class="center">
							<xsl:choose>
								<xsl:when test="Outcome/Result='discharge'">
									<xsl:text>Released</xsl:text>
								</xsl:when>
								<xsl:when test="Outcome/Result='death'">
									<xsl:text>Death</xsl:text>
								</xsl:when>
								<xsl:otherwise/>
							</xsl:choose>
						</td>
						<td class="center">
							<input type="button" onclick="window.open('{$url}','_self')" value="Metadata"/>
						</td>
						<td class="center">
							<input type="button" onclick="window.open('{$url}/export','_self')" value="Study"/>
						</td>
						<td class="center">
							<input type="button" onclick="window.open('{$url}/list','_self')" value="Images"/>
						</td>
						<xsl:if test="$admin='yes'">
							<td class="center">
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
