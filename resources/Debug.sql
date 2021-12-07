select req.id,
       req.message_id,
       req.destination_name,
       req.inquiry_version,
       cnt.content
from core.message_metadata req
         left join core.message_content cnt on
    req.id = cnt.id
         left join core.message_metadata resp on
    resp.reference_id = req.id
where req.message_type = 'REQUEST'
  and req.inquiry_version = 'urn://gosuslugi/reg-m2m/1.0.1'
order by req.delivery_date
limit 1;