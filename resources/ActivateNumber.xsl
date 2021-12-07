<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	<xsl:param name="Mnemonic"/>
	<xsl:param name="СlientID"/>
	<xsl:param name="ReplyTo"/>
	<xsl:param name="Status"/>
	<xsl:param name="OrderID"/>
	<xsl:param name="Comment"/>
	<xsl:template match="/">
		<tns:ClientMessage xmlns:tns="urn://x-artefacts-smev-gov-ru/services/service-adapter/types">
			<tns:itSystem>
				<xsl:copy-of select="$Mnemonic"/>
			</tns:itSystem>
			<tns:ResponseMessage>
				<tns:ResponseMetadata>
					<tns:clientId>
						<xsl:copy-of select="$СlientID"/>
					</tns:clientId>
					<tns:replyToClientId>
						<xsl:copy-of select="$ReplyTo"/>
					</tns:replyToClientId>
				</tns:ResponseMetadata>
				<tns:ResponseContent>
					<tns:content>
						<tns:MessagePrimaryContent>
							<tns:ResponseActivateNumber xmlns:tns="urn://gosuslugi/activate-number/1.0.1">
								<tns:changeOrderInfo>
									<tns:orderId>
										<xsl:copy-of select="$OrderID"/>
									</tns:orderId>
									<tns:statusCode>
										<tns:techCode>
											<xsl:copy-of select="$Status"/>
										</tns:techCode>
									</tns:statusCode>
									<xsl:if test="string-length($Comment) != 0">
										<tns:comment>
											<xsl:copy-of select="$Comment"/>
										</tns:comment>
									</xsl:if>
								</tns:changeOrderInfo>
							</tns:ResponseActivateNumber>
						</tns:MessagePrimaryContent>
					</tns:content>
				</tns:ResponseContent>
			</tns:ResponseMessage>
		</tns:ClientMessage>
	</xsl:template>
</xsl:stylesheet>
