CREATE TABLE `score_modification_log` (
	`score_modification_log_id`	BIGINT	NOT NULL,
	`score_evaluatee_id`	BIGINT	NULL,
	`score_modifier_id`	BIGINT	NULL	COMMENT 'HRM',
	`score_original_score`	DECIMAL	NULL,
	`score_modified_score`	DECIMAL	NULL,
	`score_reason`	VARCHAR (500)	NULL	COMMENT '10~500자',
	`score_is_deletable`	BOOLEAN	NULL	COMMENT '항상 false',
	`score_modified_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	TIMESTAMP	NULL,
	`updated_at`	BIGINT	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `employee` (
	`employee_id`	BIGINT	NOT NULL,
	`employee_code`	VARCHAR(255)	NULL,
	`employee_name`	VARCHAR(255)	NULL,
	`employee_email`	VARCHAR(255)	NULL,
	`employee_phone`	VARCHAR(255)	NULL,
	`employee_address`	VARCHAR(255)	NULL,
	`employee_emergency_contact`	VARCHAR(30)	NULL,
	`employee_password`	VARCHAR(255)	NULL,
	`employee_role`	ENUM(worker,TL,DL,HRM,admin)	NULL,
	`employee_status`	ENUM(재직,휴직,퇴사)	NULL,
	`mfa_enabled`	BOOLEAN	NULL,
	`login_fail_count`	INT	NULL,
	`is_locked`	BOOLEAN	NULL,
	`last_login_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL,
	`department_id`	BIGINT	NOT NULL
);

CREATE TABLE `consent` (
	`consent_id`	BIGINT	NOT NULL,
	`employee_id`	BIGINT	NOT NULL,
	`consent_version`	VARCHAR(10)	NULL,
	`consent_saved_path`	VARCHAR(255)	NULL,
	`is_agreed`	BOOLEAN	NULL,
	`consented_at`	DATE	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `score` (
	`score_id`	BIGINT	NOT NULL,
	`employee_id`	BIGINT	NOT NULL,
	`evaluation_year`	INT	NULL,
	`evaluation_period`	VARCHAR(50)	NULL,
	`capability_index`	DECIMAL	NULL,
	`total_points`	INT	NULL,
	`tier`	ENUM(S/A/B/C)	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `performance_point` (
	`performance_point_id`	BIGINT	NOT NULL,
	`performance_employee_id`	BIGINT	NOT NULL,
	`point_type`	ENUM	NULL	COMMENT '정량,정성,지식공유,도전',
	`point_amount`	DECIMAL	NULL,
	`point_earned_date`	DATE	NULL,
	`point_source_id`	BIGINT	NULL	COMMENT '평가,지식,주문등',
	`point_source_type`	VARCHAR (255)	NULL,
	`point_description`	VARCHAR (500)	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `maintenance_log` (
	`maintenance_log_id`	BIGINT	NOT NULL,
	`equipment_id`	BIGINT	NOT NULL,
	`maintenance_item_standard_id`	BIGINT	NOT NULL,
	`maintenance_type`	ENUM	NULL	COMMENT '정기//비정기',
	`maintenance_date`	DATE	NULL,
	`maintenance_score`	DECIMAL	NULL	COMMENT '0~100',
	`eta_maint_delta`	DECIMAL	NULL	COMMENT 'n_maint 변화량',
	`maintenance_result`	ENUM	NULL	COMMENT '정상/이상감지/수리필요/수리완료',
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `factory_line` (
	`factory_line_id`	BIGINT	NOT NULL,
	`factory_line_code`	VARCHAR(255)	NOT NULL	COMMENT '예) Line-1, Line-A',
	`factory_line_name`	VARCHAR(255)	NULL	COMMENT '예) 1라인 2라인, A라인',
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `qualitative_evaluation` (
	`qualitative_evaluation_id`	BIGINT	NOT NULL,
	`evaluatee_id`	BIGINT	NOT NULL,
	`evaluator_id`	BIGINT	NOT NULL,
	`evaluation_period_id`	BIGINT	NOT NULL,
	`evaluation_level`	VARCHAR(255)	NULL,
	`eval_items`	JSON	NULL,
	`eval_comment`	VARCHAR(2000)	NULL	COMMENT '최소 20자, 최대 2000자',
	`grade`	ENUM	NULL	COMMENT 'S,A,B,C',
	`score`	DECIMAL	NULL	COMMENT '0~100  1차 평가',
	`s_qual`	DECIMAL	NULL	COMMENT '0~100  2차 평가',
	`input_method`	ENUM	NULL	COMMENT '텍스트|음성STT',
	`status`	ENUM	NULL	COMMENT '미입력|입력완료|수정됨|1차확정|2차확정|최종확정',
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `equipment_process` (
	`equipment_process_id`	BIGINT	NOT NULL,
	`factory_line_id`	BIGINT	NOT NULL,
	`equipment_process_code`	VARCHAR(255)	NULL,
	`equipment_process_name`	VARCHAR(255)	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `knowledge_article` (
	`article_id`	BIGINT	NOT NULL,
	`author_id`	BIGINT	NOT NULL,
	`approved_by`	BIGINT	NULL,
	`equipment_id`	BIGINT	NOT NULL,
	`file_group_id`	BIGINT	NOT NULL,
	`article_title`	VARCHAR(255)	NULL	COMMENT '5~200자',
	`article_category`	ENUM	NULL	COMMENT '장애조치, 공정개선, 설비운영, 안전 기타',
	`article_content`	TEXT	NULL	COMMENT '50~10000자, HTML/마크다운',
	`article_status`	ENUM	NULL	COMMENT 'Draft, Pending, Approved, Rejected',
	`article_approval_opinion`	VARCHAR(500)	NULL	COMMENT '최대 500자',
	`approved_at`	TIMESTAMP	NULL,
	`article_rejection_reason`	VARCHAR(500)	NULL	COMMENT '10~500자',
	`article_deletion_reason`	VARCHAR(500)	NULL	COMMENT '10~500자',
	`deleted_at`	TIMESTAMP	NULL,
	`is_deleted`	BOOLEAN	NULL,
	`view_count`	INT	NULL	DEFAULT 0,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `notification` (
	`notification_id`	BIGINT	NOT NULL,
	`employee_id`	BIGINT	NOT NULL	COMMENT '수정 필요',
	`notification_type`	ENUM	NULL	COMMENT '평가결과 | 이의처리 | 승급 | 배치 | 편향감지',
	`notification_title`	VARCHAR(255)	NULL,
	`notification_content`	TEXT	NULL,
	`notification_is_read`	BOOLEAN	NULL,
	`notification_sent_at`	TIMESTAMP	NULL,
	`notification_read_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `maintenance_item_standard` (
	`maintenance_item_standard_id`	BIGINT	NOT NULL,
	`maintenance_item`	VARCHAR(255)	NULL	COMMENT '청소, 소모품교체, 정기점검, 긴급수리',
	`maintenance_weight`	DECIMAL	NULL	COMMENT '항목 가중치',
	`maintenance_score_max`	DECIMAL	NULL	COMMENT '항목 최대 점수',
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `anti_gaming_flag` (
	`flag_id`	BIGINT	NOT NULL,
	`employee_id`	BIGINT	NOT NULL,
	`production_speed_rank`	INT	NULL	COMMENT '상위25%',
	`safety_keyword_rank`	INT	NULL	COMMENT '하위25%',
	`penalty_coefficient`	DECIMAL	NULL,
	`target_year`	INT	NULL,
	`target_period`	VARCHAR(50)	NULL,
	`is_active`	TINYINT	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `notice` (
	`Field`	VARCHAR(255)	NULL
);

CREATE TABLE `environment_event` (
	`environment_event_id`	BIGINT	NOT NULL,
	`equipment_id`	BIGINT	NOT NULL,
	`env_temperature`	DECIMAL	NULL,
	`env_humidity`	DECIMAL	NULL,
	`env_particle_cnt`	INT	NULL,
	`env_deviation_type`	ENUM	NULL	COMMENT '온도이탈/습도이탈/파티클이탈',
	`env_correction_applied`	BOOLEAN	NULL,
	`env_detected_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `evaluation_period` (
	`eval_period_id`	BIGINT	NOT NULL,
	`algorithm_version_id`	BIGINT	NOT NULL,
	`eval_year`	INT	NOT NULL	COMMENT 'YYYY',
	`eval_sequence`	INT	NOT NULL	COMMENT '분기1<4 / 반기 1>2',
	`eval_type`	ENUM(정량, 정성, 종합)	NOT NULL	COMMENT '정량|정성|종합',
	`start_date`	DATE	NOT NULL,
	`end_date`	DATE	NOT NULL,
	`status`	ENUM(진행중,마감,확정)	NOT NULL	COMMENT '진행중|마감|확정',
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `quantitative_evaluation` (
	`quantitative_evaluation_id`	BIGINT	NOT NULL,
	`employee_id`	BIGINT	NOT NULL,
	`eval_period_id`	BIGINT	NOT NULL,
	`equipment_id`	BIGINT	NOT NULL,
	`uph_score`	DECIMAL	NULL	COMMENT '목표 달성도',
	`yield_score`	DECIMAL	NULL	COMMENT '품질 합격률',
	`lead_time_score`	DECIMAL	NULL	COMMENT '공정 효율성',
	`actual_error`	DECIMAL	NULL,
	`s_quant`	DECIMAL	NULL,
	`t_score`	DECIMAL	NULL,
	`material_shielding`	TINYINT	NULL,
	`status`	ENUM	NULL	COMMENT '임시|확정',
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `knowledge_edit_history` (
	`history_id`	BIGINT	NOT NULL,
	`article_id`	BIGINT	NOT NULL,
	`editor_id`	BIGINT	NOT NULL,
	`article_previous_content`	TEXT	NULL,
	`article_new_content`	TEXT	NULL,
	`edited_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL,
	`Key2`	VARCHAR(255)	NOT NULL
);

CREATE TABLE `OCSA_weight_config` (
	`config_id`	BIGINT	NOT NULL,
	`industry_preset`	ENUM	NULL	COMMENT '반도체, 디스플레이, 배터리, 자동차, 기타, 커스텀',
	`weight_v1`	DECIMAL	NULL	COMMENT '합계 = 1.0',
	`weight_v2`	DECIMAL	NULL,
	`weight_v3`	DECIMAL	NULL,
	`weight_v4`	DECIMAL	NULL,
	`alpha_weight`	DECIMAL	NULL	COMMENT '0.0~0.5',
	`effective_date`	DATE	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `bias_correction` (
	`bias_correction_id`	BIGINT	NOT NULL,
	`evaluator_id`	BIGINT	NOT NULL,
	`qualitative_evaluation_id`	BIGINT	NOT NULL,
	`bias_type`	ENUM	NOT NULL	COMMENT 'Strict/Generous',
	`evaluator_avg`	DECIMAL	NOT NULL,
	`company_avg`	DECIMAL	NOT NULL,
	`alpha_bias`	DECIMAL	NOT NULL,
	`original_score`	DECIMAL	NOT NULL,
	`corrected_score`	DECIMAL	NOT NULL,
	`notification_sent`	TINYINT	NOT NULL,
	`detected_at`	TIMESTAMP	NOT NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `environment_standard` (
	`environment_standard_id`	BIGINT	NOT NULL,
	`environment_type`	ENUM(DRYROOM/CLEANROOM/일반)	NULL,
	`env_temp_min`	DECIMAL	NULL,
	`env_temp_max`	DECIMAL	NULL,
	`env_humidity_min`	DECIMAL	NULL	COMMENT '0~100',
	`env_humidity_max`	DECIMAL	NULL	COMMENT '0~100',
	`env_particle_limit`	INT	NULL	COMMENT '개/m3',
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `equipment` (
	`equipment_id`	BIGINT	NOT NULL,
	`equipment_process_id`	BIGINT	NOT NULL,
	`environment_standard_id`	BIGINT	NOT NULL,
	`equipment_code`	VARCHAR(255)	NULL	COMMENT '영문+숫자',
	`equipment_name`	VARCHAR(255)	NULL,
	`equipment_status`	ENUM	NULL	COMMENT '가동/정지/점검/폐기',
	`equipment_grade`	ENUM	NULL	COMMENT 'S/A/B/C',
	`equipment_install_date`	TIMESTAMP	NULL	COMMENT 'SAT완료일',
	`equipment_description`	VARCHAR(255)	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `promotion_history` (
	`tier_promotion_id`	BIGINT	NOT NULL,
	`employee_id`	BIGINT	NOT NULL,
	`reviewer_id`	BIGINT	NOT NULL	COMMENT 'HR Manager',
	`current_tier_config_id`	BIGINT	NOT NULL,
	`target_tier_config_id`	BIGINT	NOT NULL,
	`tier_config_effective_date`	DATE	NULL,
	`tier_accumulated_point`	INT	NULL,
	`tier_promo_status`	ENUM	NULL	COMMENT '심사중 | 승급확정 | 보류',
	`tier_reviewed_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `department` (
	`department_id`	BIGINT	NOT NULL,
	`parent_department_id`	BIGINT	NOT NULL,
	`department_name`	VARCHAR(30)	NULL,
	`team_name`	VARCHAR(255)	NULL,
	`depth`	VARCHAR(255)	NULL	COMMENT 'Root, L1, L2, L3 ...',
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `knowledge_tag` (
	`tag_id`	BIGINT	NOT NULL,
	`tag_name`	VARCHAR(255)	NOT NULL
);

CREATE TABLE `access_log` (
	`access_log_id`	BIGINT	NOT NULL,
	`employee_id`	BIGINT	NOT NULL,
	`ip_address`	VARCHAR(255)	NULL,
	`action`	VARCHAR(255)	NULL,
	`uri`	VARCHAR(255)	NULL,
	`is_authorized`	BOOLEAN	NULL,
	`access_time`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `product` (
	`product_id`	BIGINT	NOT NULL,
	`product_name`	VARCHAR(255)	NULL,
	`product_code`	VARCHAR(255)	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `role_change_history` (
	`role_change_history_id`	BIGINT	NOT NULL,
	`target_employee_id`	BIGINT	NOT NULL,
	`changed_by`	BIGINT	NOT NULL,
	`previous_role`	ENUM	NULL,
	`new_role`	ENUM	NULL,
	`reason`	VARCHAR(255)	NULL,
	`effective_date`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL
);

CREATE TABLE `attachment_file_group` (
	`file_group_id`	BIGINT	NOT NULL,
	`reference_id`	BIGINT	NULL,
	`reference_type`	ENUM	NULL	COMMENT 'APPEAL | KNOWLEDGE | EVALUATION'
);

CREATE TABLE `attachment` (
	`attachment_id`	BIGINT	NOT NULL,
	`file_group_id`	BIGINT	NOT NULL,
	`file_name`	VARCHAR(255)	NULL,
	`file_path`	VARCHAR(255)	NULL,
	`file_size`	BIGINT	NULL,
	`file_type`	ENUM	NULL	COMMENT 'pdf | jpg | png | xlsx | mp4 | wav | mp3',
	`file_attachment_uploaded_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `worker_deployment` (
	`worker_deployment_id`	BIGINT	NOT NULL,
	`employee_id`	BIGINT	NOT NULL,
	`equipment_id`	BIGINT	NOT NULL,
	`worker_deployment_role`	ENUM	NULL	COMMENT '주담당/보조/교육/대기',
	`start_date`	DATE	NULL,
	`end_date`	DATE	NULL,
	`shift`	ENUM	NULL	COMMENT 'A/B/C/상시',
	`allocation_rate`	INT	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `data_destruction_log` (
	`data_destruction_log_id`	BIGINT	NOT NULL,
	`employee_id`	BIGINT	NULL,
	`destruction_type`	ENUM	NULL	COMMENT '정기/즉시',
	`destruction_method`	ENUM	NULL,
	`executor_id`	BIGINT	NULL,
	`retention_expired`	BOOLEAN	NULL,
	`data_destroyed_at`	DATE	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `batch_execution` (
	`batch_execution_id`	BIGINT	NOT NULL,
	`batch_schedule_id`	BIGINT	NOT NULL,
	`executor_id`	BIGINT	NULL	COMMENT '수동 시',
	`batch_type`	ENUM	NULL	COMMENT '주간 | 월간 | 연간',
	`trigger_type`	ENUM	NULL	COMMENT '자동 / 수동',
	`batch_scope`	ENUM	NULL	COMMENT '전사 | 부서별 | 팀별',
	`batch_reason`	VARCHAR(500)	NULL	COMMENT '10~500자',
	`processed_count`	INT	NULL,
	`batch_result`	ENUM	NULL	COMMENT '성공 | 실패 | 부분성공',
	`is_idempotent`	BOOLEAN	NULL,
	`batch_execution_started_at`	TIMESTAMP	NULL,
	`batch_execution_completed_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `domain_keyword` (
	`domain_keyword_id`	BIGINT	NOT NULL,
	`domain_keyword`	VARCHAR(50)	NULL	COMMENT '2~50자',
	`domain_competency_category`	ENUM	NULL	COMMENT '기술역량, 리더십, 안전, 혁신, 협업, 기타',
	`domain_base_score`	DECIMAL	NULL	COMMENT '0.1~10.0',
	`domain_weight`	DECIMAL	NULL	COMMENT '1.0~5.0',
	`domain_is_active`	BOOLEAN	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `material_defect_event` (
	`material_defect_event_id`	BIGINT	NOT NULL,
	`lot_id`	VARCHAR (255)	NULL,
	`line_ids`	JSON	NULL,
	`defect_rates`	JSON	NULL,
	`detection_threshold`	DECIMAL	NULL,
	`affected_worker_ids`	JSON	NULL,
	`shielding_applied`	BOOLEAN	NULL,
	`point_deferred`	BOOLEAN	NULL,
	`material_detected_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `equipment_baseline` (
	`equipment_baseline_id`	BIGINT	NOT NULL,
	`equipment_id`	BIGINT	NOT NULL,
	`equipment_aging_param_id`	BIGINT	NOT NULL	COMMENT '장비 노후도를 위한 참조',
	`equipment_standard_performance_rate`	DECIMAL	NULL,
	`equipment_baseline_error_rate`	DECIMAL	NULL,
	`equipment_eta_maint`	DECIMAL	NULL,
	`equipment_idx`	DECIMAL	NULL,
	`equipment_baseline_calculated_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `skill` (
	`skill_id`	BIGINT	NOT NULL,
	`employee_id`	BIGINT	NOT NULL,
	`skill_category`	ENUM	NULL	COMMENT 'unique 설비대응, 기술전수, 혁신제안, 안전준수, 품질관리, 생산성',
	`skill_score`	DECIMAL	NULL,
	`skill_tier`	ENUM	NULL	COMMENT 'S, A, B, C',
	`evaluated_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `equipment_aging_param` (
	`equipment_aging_param_id`	BIGINT	NOT NULL,
	`equipment_id`	BIGINT	NOT NULL,
	`equipment_eta_age`	DECIMAL	NULL,
	`equipment_warranty_month`	INT	NULL,
	`equipment_design_life_months`	INT	NULL,
	`equipment_wear_coefficient`	DECIMAL	NULL	COMMENT 'lambda(0.01~1.00)',
	`equipment_age_months`	INT	NULL	COMMENT '배치 실행 시점, 보증기간으로 시간 계산',
	`equipment_age_calculated_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `tier_config` (
	`tier_config_id`	BIGINT	NOT NULL,
	`tier_config_tier`	ENUM	NULL	COMMENT 'S | A | B | C',
	`tier_config_weight_distribution`	JSON	NULL	COMMENT '합계 100%',
	`tier_config_promotion_point`	INT	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `algorithm_version` (
	`algorithm_version_id`	BIGINT	NOT NULL,
	`version_no`	VARCHAR(255)	NULL	COMMENT 'v1.0, v1.1...',
	`formula`	VARCHAR(255)	NULL,
	`parameters`	JSON	NULL	COMMENT '키-값',
	`reference_values`	JSON	NULL	COMMENT 'min/max',
	`effective_date`	DATE	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `matching_record` (
	`matching_record_id`	BIGINT	NOT NULL,
	`employee_id`	BIGINT	NOT NULL,
	`order_id`	BIGINT	NOT NULL,
	`matching_mode`	ENUM	NULL	COMMENT '성장형(D>C), 효율형(D~C)',
	`matching_status`	ENUM	NULL	COMMENT '추천, 신청, 승인, 확정, 거절, 완료',
	`d_c_ratio`	DECIMAL	NULL,
	`expected_bonus`	DECIMAL	NULL,
	`expected_productivity`	DECIMAL	NULL,
	`quality_risk`	DECIMAL	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `orders` (
	`order_id`	BIGINT	NOT NULL,
	`product_id`	BIGINT	NOT NULL,
	`config_id`	BIGINT	NOT NULL,
	`order_no`	VARCHAR(255)	NOT NULL,
	`order_quantity`	INT	NOT NULL,
	`order_deadline`	DATE	NULL,
	`is_first_order`	BOOLEAN	NULL,
	`order_status`	ENUM	NOT NULL	COMMENT '(등록, 분석완료, 배정중, 진행중, 완료)',
	`v1_process_complexity`	DECIMAL	NULL,
	`v2_quality_precision`	DECIMAL	NULL,
	`v3_capacity_requirements`	DECIMAL	NULL,
	`v4_space_time_urgency`	DECIMAL	NULL,
	`alpha_novelty`	DECIMAL	NULL,
	`difficulty_score`	DECIMAL	NULL,
	`difficulty_grade`	ENUM	NULL	COMMENT '(D1, D2, D3, D4, D5)',
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `evaluation_comment` (
	`evaluation_comment_id`	BIGINT	NOT NULL,
	`qualitative_evaluation_id`	BIGINT	NOT NULL,
	`content`	TEXT	NOT NULL,
	`nlp_sentiment`	DECIMAL	NOT NULL	COMMENT '(-1.0~1.0)',
	`nlp_magnitude`	DECIMAL	NOT NULL,
	`nlp_entities`	JSON	NOT NULL	COMMENT '설비ID, 공정명 등',
	`nlp_entities_sentiment`	JSON	NOT NULL,
	`nlp_syntax`	JSON	NOT NULL,
	`valid_morpheme_count`	INT	NOT NULL	COMMENT '불용어 제외',
	`domain_keyword_count`	INT	NOT NULL,
	`density_level`	ENUM	NOT NULL	COMMENT 'Reject(3미만)|Warning(3~7)|Valid(8+)',
	`beta_reliability`	DECIMAL	NOT NULL,
	`context_weight`	DECIMAL	NOT NULL,
	`analyzed_at`	TIMESTAMP	NOT NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `knowledge_article_tag` (
	`tag_id`	BIGINT	NOT NULL,
	`article_id`	BIGINT	NOT NULL
);

CREATE TABLE `password_history` (
	`password_history_id`	BIGINT	NOT NULL,
	`employee_id`	BIGINT	NOT NULL,
	`password_hash`	JSON	NULL	COMMENT '마지막3개',
	`password_changed_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

CREATE TABLE `evaluation_appeal` (
	`appeal_id`	BIGINT	NOT NULL,
	`qualitative_evaluation_id`	BIGINT	NOT NULL	COMMENT '정성',
	`appeal_employee_id`	BIGINT	NOT NULL,
	`appeal_type`	ENUM(점수오류,항목누락,평가절차,기타)	NULL	COMMENT '점수오류|항목누락|평가절차|기타',
	`title`	VARCHAR(100)	NULL	COMMENT '5~100자',
	`content`	TEXT	NULL	COMMENT '20~2000자',
	`status`	ENUM(접수,검토중,처리완료)	NULL	COMMENT '접수|검토중|처리완료',
	`review_result`	ENUM(인용|일부인용|기각)	NULL	COMMENT '인용(점수수정)|일부인용|기각',
	`reviewer_id`	BIGINT	NULL,
	`anonymized_comparison`	BIGINT	NOT NULL,
	`filed_at`	TIMESTAMP	NULL	COMMENT '결과통보 후 7일이내',
	`reviewed_at`	TIMESTAMP	NULL,
	`reviewed_by`	BIGINT	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL,
	`file_group_id`	BIGINT	NOT NULL
);

CREATE TABLE `batch_schedule` (
	`batch_schedule_id`	BIGINT	NOT NULL,
	`batch_type`	ENUM	NULL	COMMENT '주간 | 월간 | 연간',
	`batch_cron_expression`	VARCHAR(255)	NULL,
	`batch_scope`	ENUM	NULL	COMMENT '전사 | 부서별 | 팀별',
	`batch_is_active`	BOOLEAN	NULL,
	`batch_schedule_created_at`	TIMESTAMP	NULL,
	`batch_schedule_updated_at`	TIMESTAMP	NULL,
	`created_at`	TIMESTAMP	NULL,
	`created_by`	BIGINT	NULL,
	`updated_at`	TIMESTAMP	NULL,
	`updated_by`	BIGINT	NULL
);

ALTER TABLE `score_modification_log` ADD CONSTRAINT `PK_SCORE_MODIFICATION_LOG` PRIMARY KEY (
	`score_modification_log_id`
);

ALTER TABLE `employee` ADD CONSTRAINT `PK_EMPLOYEE` PRIMARY KEY (
	`employee_id`
);

ALTER TABLE `consent` ADD CONSTRAINT `PK_CONSENT` PRIMARY KEY (
	`consent_id`
);

ALTER TABLE `score` ADD CONSTRAINT `PK_SCORE` PRIMARY KEY (
	`score_id`
);

ALTER TABLE `performance_point` ADD CONSTRAINT `PK_PERFORMANCE_POINT` PRIMARY KEY (
	`performance_point_id`
);

ALTER TABLE `maintenance_log` ADD CONSTRAINT `PK_MAINTENANCE_LOG` PRIMARY KEY (
	`maintenance_log_id`
);

ALTER TABLE `factory_line` ADD CONSTRAINT `PK_FACTORY_LINE` PRIMARY KEY (
	`factory_line_id`
);

ALTER TABLE `qualitative_evaluation` ADD CONSTRAINT `PK_QUALITATIVE_EVALUATION` PRIMARY KEY (
	`qualitative_evaluation_id`
);

ALTER TABLE `equipment_process` ADD CONSTRAINT `PK_EQUIPMENT_PROCESS` PRIMARY KEY (
	`equipment_process_id`
);

ALTER TABLE `knowledge_article` ADD CONSTRAINT `PK_KNOWLEDGE_ARTICLE` PRIMARY KEY (
	`article_id`
);

ALTER TABLE `notification` ADD CONSTRAINT `PK_NOTIFICATION` PRIMARY KEY (
	`notification_id`
);

ALTER TABLE `maintenance_item_standard` ADD CONSTRAINT `PK_MAINTENANCE_ITEM_STANDARD` PRIMARY KEY (
	`maintenance_item_standard_id`
);

ALTER TABLE `anti_gaming_flag` ADD CONSTRAINT `PK_ANTI_GAMING_FLAG` PRIMARY KEY (
	`flag_id`
);

ALTER TABLE `environment_event` ADD CONSTRAINT `PK_ENVIRONMENT_EVENT` PRIMARY KEY (
	`environment_event_id`
);

ALTER TABLE `evaluation_period` ADD CONSTRAINT `PK_EVALUATION_PERIOD` PRIMARY KEY (
	`eval_period_id`
);

ALTER TABLE `quantitative_evaluation` ADD CONSTRAINT `PK_QUANTITATIVE_EVALUATION` PRIMARY KEY (
	`quantitative_evaluation_id`
);

ALTER TABLE `knowledge_edit_history` ADD CONSTRAINT `PK_KNOWLEDGE_EDIT_HISTORY` PRIMARY KEY (
	`history_id`
);

ALTER TABLE `OCSA_weight_config` ADD CONSTRAINT `PK_OCSA_WEIGHT_CONFIG` PRIMARY KEY (
	`config_id`
);

ALTER TABLE `bias_correction` ADD CONSTRAINT `PK_BIAS_CORRECTION` PRIMARY KEY (
	`bias_correction_id`
);

ALTER TABLE `environment_standard` ADD CONSTRAINT `PK_ENVIRONMENT_STANDARD` PRIMARY KEY (
	`environment_standard_id`
);

ALTER TABLE `equipment` ADD CONSTRAINT `PK_EQUIPMENT` PRIMARY KEY (
	`equipment_id`
);

ALTER TABLE `promotion_history` ADD CONSTRAINT `PK_PROMOTION_HISTORY` PRIMARY KEY (
	`tier_promotion_id`
);

ALTER TABLE `department` ADD CONSTRAINT `PK_DEPARTMENT` PRIMARY KEY (
	`department_id`
);

ALTER TABLE `knowledge_tag` ADD CONSTRAINT `PK_KNOWLEDGE_TAG` PRIMARY KEY (
	`tag_id`
);

ALTER TABLE `access_log` ADD CONSTRAINT `PK_ACCESS_LOG` PRIMARY KEY (
	`access_log_id`
);

ALTER TABLE `product` ADD CONSTRAINT `PK_PRODUCT` PRIMARY KEY (
	`product_id`
);

ALTER TABLE `role_change_history` ADD CONSTRAINT `PK_ROLE_CHANGE_HISTORY` PRIMARY KEY (
	`role_change_history_id`
);

ALTER TABLE `attachment_file_group` ADD CONSTRAINT `PK_ATTACHMENT_FILE_GROUP` PRIMARY KEY (
	`file_group_id`
);

ALTER TABLE `attachment` ADD CONSTRAINT `PK_ATTACHMENT` PRIMARY KEY (
	`attachment_id`
);

ALTER TABLE `worker_deployment` ADD CONSTRAINT `PK_WORKER_DEPLOYMENT` PRIMARY KEY (
	`worker_deployment_id`
);

ALTER TABLE `data_destruction_log` ADD CONSTRAINT `PK_DATA_DESTRUCTION_LOG` PRIMARY KEY (
	`data_destruction_log_id`
);

ALTER TABLE `batch_execution` ADD CONSTRAINT `PK_BATCH_EXECUTION` PRIMARY KEY (
	`batch_execution_id`
);

ALTER TABLE `domain_keyword` ADD CONSTRAINT `PK_DOMAIN_KEYWORD` PRIMARY KEY (
	`domain_keyword_id`
);

ALTER TABLE `material_defect_event` ADD CONSTRAINT `PK_MATERIAL_DEFECT_EVENT` PRIMARY KEY (
	`material_defect_event_id`
);

ALTER TABLE `equipment_baseline` ADD CONSTRAINT `PK_EQUIPMENT_BASELINE` PRIMARY KEY (
	`equipment_baseline_id`
);

ALTER TABLE `skill` ADD CONSTRAINT `PK_SKILL` PRIMARY KEY (
	`skill_id`
);

ALTER TABLE `equipment_aging_param` ADD CONSTRAINT `PK_EQUIPMENT_AGING_PARAM` PRIMARY KEY (
	`equipment_aging_param_id`
);

ALTER TABLE `tier_config` ADD CONSTRAINT `PK_TIER_CONFIG` PRIMARY KEY (
	`tier_config_id`
);

ALTER TABLE `algorithm_version` ADD CONSTRAINT `PK_ALGORITHM_VERSION` PRIMARY KEY (
	`algorithm_version_id`
);

ALTER TABLE `matching_record` ADD CONSTRAINT `PK_MATCHING_RECORD` PRIMARY KEY (
	`matching_record_id`
);

ALTER TABLE `orders` ADD CONSTRAINT `PK_ORDERS` PRIMARY KEY (
	`order_id`
);

ALTER TABLE `evaluation_comment` ADD CONSTRAINT `PK_EVALUATION_COMMENT` PRIMARY KEY (
	`evaluation_comment_id`
);

ALTER TABLE `knowledge_article_tag` ADD CONSTRAINT `PK_KNOWLEDGE_ARTICLE_TAG` PRIMARY KEY (
	`tag_id`,
	`article_id`
);

ALTER TABLE `password_history` ADD CONSTRAINT `PK_PASSWORD_HISTORY` PRIMARY KEY (
	`password_history_id`
);

ALTER TABLE `evaluation_appeal` ADD CONSTRAINT `PK_EVALUATION_APPEAL` PRIMARY KEY (
	`appeal_id`
);

ALTER TABLE `batch_schedule` ADD CONSTRAINT `PK_BATCH_SCHEDULE` PRIMARY KEY (
	`batch_schedule_id`
);

ALTER TABLE `knowledge_article_tag` ADD CONSTRAINT `FK_knowledge_tag_TO_knowledge_article_tag_1` FOREIGN KEY (
	`tag_id`
)
REFERENCES `knowledge_tag` (
	`tag_id`
);

ALTER TABLE `knowledge_article_tag` ADD CONSTRAINT `FK_knowledge_article_TO_knowledge_article_tag_1` FOREIGN KEY (
	`article_id`
)
REFERENCES `knowledge_article` (
	`article_id`
);

