SELECT req.id, req.message_id, req.destination_name, req.inquiry_version, cnt.content
FROM core.message_metadata req
    LEFT JOIN core.message_content cnt ON req.id = cnt.id
    LEFT JOIN core.message_metadata resp ON resp.reference_id = req.id
    LEFT JOIN core.message_state ms ON ms.id = req.id
WHERE req.message_type = 'REQUEST'
  AND ms.transport_direct = 'INCOMING'
  AND req.inquiry_version IS NOT NULL
  AND resp.id IS NULL
ORDER BY req.delivery_date;