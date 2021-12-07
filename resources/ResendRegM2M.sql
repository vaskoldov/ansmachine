select mm.id, mm.message_id, mm.destination_name, mm.inquiry_version, mc.content
from core.message_metadata mm
         left join core.message_content mc on mc.id = mm.id
where mm.message_id in (
                        'c4e40a1e-48a7-11ec-90a1-426119952352',
                        '0bd2e9d5-48a8-11ec-9cba-62dd6622905e',
                        '41601904-48ff-11ec-8723-eed3546e5783',
                        '8b3aa7c2-48ff-11ec-9f6e-4e9818ca3f73',
                        '5c6cb9b0-4916-11ec-a974-8a8b479c92ee',
                        '323983b5-491a-11ec-9cba-62dd6622905e',
                        'b15053f4-4922-11ec-976a-b6a616e8c8a7',
                        'be70d8e6-493a-11ec-8723-eed3546e5783',
                        'd14b72ed-4940-11ec-90a1-426119952352',
                        '6997122c-4b5e-11ec-b001-227b847d0fe8',
                        'e174c51f-4b6c-11ec-b7de-6a55da72803b',
                        'ee246ee0-4b73-11ec-bf46-a2b1bf7a9b8b',
                        '026528ae-4dc6-11ec-b066-aee1000a6ea6',
                        '64ba0a57-4dc6-11ec-b066-aee1000a6ea6',
                        '8d134140-4dc8-11ec-a9df-720450644a06',
                        '74826d5a-4dca-11ec-a624-96cbeae772ef',
                        '9472b2b3-4dca-11ec-a624-96cbeae772ef',
                        'ce9d725c-4dca-11ec-a562-5658c206d62e',
                        '1fabfd0a-4de4-11ec-9b0a-b28caf4ad85e',
                        '5e1142af-4ded-11ec-a9df-720450644a06',
                        '8f1911c1-4def-11ec-9b0a-b28caf4ad85e',
                        'ff48c9a1-4df1-11ec-a624-96cbeae772ef',
                        'c9a5d9f4-4e81-11ec-a6cc-1e7a83b0af0e',
                        '412791bc-4e82-11ec-ac02-3276be3aafef',
                        'f7932967-4e8b-11ec-868a-3a3e6cc93b6f',
                        'e32ac92a-4e8d-11ec-a588-120c70441959',
                        '189f5391-4e8e-11ec-b950-62e143025997',
                        '28b277fd-4e8e-11ec-b950-62e143025997',
                        'd71ebbfe-4e92-11ec-ac02-3276be3aafef',
                        '625ea980-4e94-11ec-b2ee-7ed3901c93ea',
                        '017ca4ef-4eac-11ec-b2ee-7ed3901c93ea',
                        '3dcddbc6-4eae-11ec-a588-120c70441959',
                        '6208e9c3-4eae-11ec-ba0f-12025ce7ee09',
                        '3a1de0ff-4eb0-11ec-aa96-8eb4c9c80c08',
                        '37f16edd-4eb1-11ec-99e0-9ac46db06fdc',
                        '9df256c7-4eb3-11ec-a6cc-1e7a83b0af0e',
                        'd4a992c7-4eb6-11ec-ac02-3276be3aafef',
                        '3f6d7e76-4eda-11ec-8228-ba732b590c32',
                        'cee0c379-4f4f-11ec-ae44-9a11f3113b72',
                        'f0a64707-50e3-11ec-9214-1ab3ac8fd452',
                        'a9784231-50eb-11ec-b493-eed62980fd11',
                        'e8ca672b-50ee-11ec-a734-1ae858dd9815',
                        'b4ab05c1-50f1-11ec-ac61-62823d57b5e5',
                        '6385f09f-50f3-11ec-908e-26ed2c31793c',
                        '2e53d485-50f8-11ec-90b0-02e0bb89d2f4',
                        '6136da02-50ff-11ec-a734-1ae858dd9815',
                        '8af160c5-50ff-11ec-aca9-26fe616182a6',
                        '8d211cb0-5100-11ec-9214-1ab3ac8fd452',
                        'a0d7a6e0-5100-11ec-b3c7-4e6a6c77533b',
                        '6d787d46-5102-11ec-aca9-26fe616182a6',
                        '9a33244a-5102-11ec-b3c7-4e6a6c77533b',
                        '1e529b42-5101-11ec-ac61-62823d57b5e5',
                        '32cc4711-5104-11ec-908e-26ed2c31793c',
                        '672c37ab-5105-11ec-a734-1ae858dd9815',
                        '824419cb-5105-11ec-aca9-26fe616182a6',
                        '86bc33f4-5105-11ec-aca9-26fe616182a6',
                        '57f32a16-5106-11ec-b3c7-4e6a6c77533b',
                        '4e77aeff-5107-11ec-946a-76bbc03965a2',
                        '11d67c35-5108-11ec-946a-76bbc03965a2',
                        '8910f269-5108-11ec-90b0-02e0bb89d2f4',
                        'de48e546-5108-11ec-ac61-62823d57b5e5',
                        'f66b2d73-5108-11ec-aca9-26fe616182a6',
                        '88a0b99d-510a-11ec-908e-26ed2c31793c',
                        'a4bdb97b-510b-11ec-84ae-aa19e42a3b09',
                        '0fa1cbb5-5114-11ec-b493-eed62980fd11',
                        'a77bd1ad-5117-11ec-946a-76bbc03965a2',
                        '1cafa88d-5120-11ec-b3c7-4e6a6c77533b',
                        'bb1f4798-5122-11ec-84ae-aa19e42a3b09',
                        '9b19c8b1-5129-11ec-908e-26ed2c31793c',
                        '8fd76cf3-5131-11ec-84ae-aa19e42a3b09',
                        '81cb17e1-5135-11ec-90b0-02e0bb89d2f4',
                        '6b1dee25-5136-11ec-a734-1ae858dd9815',
                        '5ecce630-519b-11ec-8c24-42dc301dc40c',
                        '29be4233-519e-11ec-a44a-b2dadab35a14',
                        '337ee767-519e-11ec-be29-922f0e325151',
                        '41853a36-51a0-11ec-a44a-b2dadab35a14',
                        '40f95812-51a1-11ec-be29-922f0e325151',
                        '545fed82-51a1-11ec-8230-a255c5b3e702',
                        'e08ca1da-51a1-11ec-8971-122634274be5',
                        'aaa86225-51a8-11ec-821b-860cc14adf15',
                        '10423fc5-51aa-11ec-8971-122634274be5',
                        '4dac55d8-51ab-11ec-be29-922f0e325151',
                        '4f86f7a1-51ad-11ec-8c24-42dc301dc40c',
                        'af214d4c-51ad-11ec-821b-860cc14adf15',
                        '5c27d1a6-51b0-11ec-8c24-42dc301dc40c',
                        '6b18c80e-51b1-11ec-b203-ea0f1edb4e70',
                        '5577411f-51b2-11ec-8c24-42dc301dc40c',
                        'ea679c53-51b2-11ec-88c9-a2971a2bcf5d',
                        'f5f55fda-51b2-11ec-8971-122634274be5',
                        'adf31e95-51b3-11ec-8230-a255c5b3e702',
                        'e5835d16-51b3-11ec-b203-ea0f1edb4e70',
                        'fb8361a1-51b3-11ec-8230-a255c5b3e702',
                        'c39e57b1-51b4-11ec-8971-122634274be5',
                        'c97d273d-51b7-11ec-91bf-ae787bf91532',
                        '7d005a69-51b8-11ec-8230-a255c5b3e702',
                        'b23b5ded-51b8-11ec-88c9-a2971a2bcf5d',
                        'dee742b0-51b9-11ec-88c9-a2971a2bcf5d',
                        'fde67ab8-51ba-11ec-be29-922f0e325151',
                        '78bc7cbc-51bd-11ec-8230-a255c5b3e702',
                        '8aebf18d-51bd-11ec-821b-860cc14adf15',
                        'aa31eceb-51bd-11ec-bd54-0abb3c5e2963',
                        '4480a384-51be-11ec-b203-ea0f1edb4e70',
                        '3e33fb33-51bf-11ec-8971-122634274be5',
                        'fff372fa-51c3-11ec-b203-ea0f1edb4e70',
                        'b2c8c81a-51c6-11ec-88c9-a2971a2bcf5d',
                        'f4530e26-51ca-11ec-91bf-ae787bf91532',
                        'acc718ee-51cc-11ec-a44a-b2dadab35a14',
                        'b82c7306-51d4-11ec-8c24-42dc301dc40c',
                        'e5c45b16-51d4-11ec-8230-a255c5b3e702',
                        '9999fde0-51d6-11ec-91bf-ae787bf91532',
                        '94646b0e-51d7-11ec-91bf-ae787bf91532',
                        '34802924-51d9-11ec-b203-ea0f1edb4e70',
                        '2e5a102a-51d9-11ec-821b-860cc14adf15',
                        '032d97af-51db-11ec-821b-860cc14adf15',
                        'e1afcdb7-51dd-11ec-8971-122634274be5',
                        '7f06edf1-51df-11ec-88c9-a2971a2bcf5d',
                        'a097d806-51e1-11ec-91bf-ae787bf91532',
                        'c3e1f674-51e1-11ec-88c9-a2971a2bcf5d',
                        'd3ad206f-51e2-11ec-8230-a255c5b3e702',
                        '1553c42a-51e3-11ec-88c9-a2971a2bcf5d',
                        '86fa8b61-51e5-11ec-8971-122634274be5',
                        'e16def8e-51ed-11ec-8971-122634274be5',
                        '2a59b052-51f9-11ec-a44a-b2dadab35a14',
                        '9880eab3-5266-11ec-be29-922f0e325151',
                        'b618ab1f-526f-11ec-91bf-ae787bf91532',
                        'ae1b9ede-528b-11ec-a2bb-f6b73cef5774',
                        'cb1a7fc2-5360-11ec-a57e-3e8ad06676b5',
                        'f22675ff-5362-11ec-ac1c-a6b08df1fcdf',
                        'b8ea31d0-5408-11ec-9fba-fe8767680c0a'
    )