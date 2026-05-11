-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 07, 2026 at 11:44 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `tlotlisangkhutlang2333874`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `auto_close_tenders` ()   BEGIN
    UPDATE tenders
    SET status = 'Closed',
        closed_date = NOW()
    WHERE status = 'Open'
      AND submission_deadline < NOW();
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `audit_log`
--

CREATE TABLE `audit_log` (
  `log_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `action` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `audit_log`
--

INSERT INTO `audit_log` (`log_id`, `user_id`, `action`, `description`, `ip_address`, `created_at`) VALUES
(12, 1, 'TENDER_STATUS_CHANGE', 'Tender MPW-2026-0001 changed from Draft to Open', NULL, '2026-05-02 21:50:25'),
(13, 1, 'TENDER_STATUS_CHANGE', 'Tender MPW-2026-0001 changed from Open to Closed', NULL, '2026-05-02 22:01:14'),
(14, 1, 'TENDER_STATUS_CHANGE', 'Tender MPW-2026-0001 changed from Closed to Under_Evaluation', NULL, '2026-05-02 22:01:22'),
(15, 1, 'TENDER_STATUS_CHANGE', 'Tender MPW-2026-0001 changed from Under_Evaluation to Evaluated', NULL, '2026-05-02 22:04:52'),
(16, 1, 'TENDER_STATUS_CHANGE', 'Tender MPW-2026-0001 changed from Evaluated to Awarded', NULL, '2026-05-02 23:06:46'),
(17, 1, 'TENDER_STATUS_CHANGE', 'Tender MPW-2026-0002 changed from Draft to Open', NULL, '2026-05-07 18:42:03'),
(18, 1, 'TENDER_STATUS_CHANGE', 'Tender MPW-2026-0002 changed from Open to Closed', NULL, '2026-05-07 18:46:17'),
(19, 1, 'TENDER_STATUS_CHANGE', 'Tender MPW-2026-0002 changed from Closed to Under_Evaluation', NULL, '2026-05-07 18:46:42'),
(20, 1, 'TENDER_STATUS_CHANGE', 'Tender MPW-2026-0002 changed from Under_Evaluation to Evaluated', NULL, '2026-05-07 18:47:38'),
(21, 1, 'TENDER_STATUS_CHANGE', 'Tender MPW-2026-0002 changed from Evaluated to Awarded', NULL, '2026-05-07 18:49:32');

-- --------------------------------------------------------

--
-- Table structure for table `awards`
--

CREATE TABLE `awards` (
  `award_id` int(11) NOT NULL,
  `tender_id` int(11) NOT NULL,
  `winning_bid_id` int(11) NOT NULL,
  `winning_supplier_id` int(11) NOT NULL,
  `awarded_value` decimal(15,2) NOT NULL,
  `justification` text NOT NULL,
  `awarded_by` int(11) DEFAULT NULL,
  `award_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `notice_generated` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `awards`
--

INSERT INTO `awards` (`award_id`, `tender_id`, `winning_bid_id`, `winning_supplier_id`, `awarded_value`, `justification`, `awarded_by`, `award_date`, `notice_generated`) VALUES
(1, 8, 10, 12, 185999999.99, 'cbcb fbfgb ffgbfg fgfg', 1, '2026-05-02 23:06:46', 0),
(2, 9, 13, 12, 20000000.00, 'Both experience and tools needed are there', 1, '2026-05-07 18:49:32', 0);

-- --------------------------------------------------------

--
-- Table structure for table `bids`
--

CREATE TABLE `bids` (
  `bid_id` int(11) NOT NULL,
  `tender_id` int(11) NOT NULL,
  `supplier_id` int(11) NOT NULL,
  `bid_amount` decimal(15,2) NOT NULL,
  `technical_statement` varchar(600) NOT NULL,
  `delivery_timeline` int(11) NOT NULL,
  `supporting_doc_path` varchar(500) NOT NULL,
  `submission_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `is_active` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bids`
--

INSERT INTO `bids` (`bid_id`, `tender_id`, `supplier_id`, `bid_amount`, `technical_statement`, `delivery_timeline`, `supporting_doc_path`, `submission_date`, `is_active`) VALUES
(10, 8, 12, 185999999.99, 'We have experience dealing with mountain roads', 344, 'bids\\1777759054926_12_1777420563356_C7-ADJ-11_End_Assessment_QP-ASR-001_Exam_Question_Paper.pdf', '2026-05-02 21:57:34', 1),
(11, 8, 11, 203000133.94, 'We are equipped with every machine needed for the project', 415, 'bids\\1777759219141_11_1777420563356_C7-ADJ-11_End_Assessment_QP-ASR-001_Exam_Question_Paper.pdf', '2026-05-02 22:00:19', 1),
(12, 9, 11, 30000000.00, 'All equipment needed available', 60, 'bids\\1778179411889_11_1777420563356_C7-ADJ-11_End_Assessment_QP-ASR-001_Exam_Question_Paper.pdf', '2026-05-07 18:43:31', 1),
(13, 9, 12, 20000000.00, 'Both experience and equipment required available', 50, 'bids\\1778179527566_12_1777420563356_C7-ADJ-11_End_Assessment_QP-ASR-001_Exam_Question_Paper.pdf', '2026-05-07 18:45:27', 1);

-- --------------------------------------------------------

--
-- Table structure for table `email_notifications`
--

CREATE TABLE `email_notifications` (
  `notification_id` int(11) NOT NULL,
  `tender_id` int(11) NOT NULL,
  `recipient_email` varchar(100) NOT NULL,
  `recipient_type` enum('WINNER','LOSER') NOT NULL,
  `subject` varchar(200) NOT NULL,
  `message` text NOT NULL,
  `sent_status` enum('PENDING','SENT','FAILED') DEFAULT 'PENDING',
  `sent_date` timestamp NULL DEFAULT NULL,
  `retry_count` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `evaluations`
--

CREATE TABLE `evaluations` (
  `id` int(11) NOT NULL,
  `tender_id` int(11) NOT NULL,
  `bid_id` int(11) NOT NULL,
  `evaluator_id` int(11) NOT NULL,
  `price_score` decimal(5,2) DEFAULT NULL,
  `technical_score` decimal(5,2) DEFAULT NULL,
  `final_score` decimal(5,2) DEFAULT NULL,
  `submitted` tinyint(1) DEFAULT 1,
  `delivery_score` decimal(5,2) DEFAULT NULL,
  `weighted_total` decimal(5,2) DEFAULT NULL,
  `submitted_date` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `evaluations`
--

INSERT INTO `evaluations` (`id`, `tender_id`, `bid_id`, `evaluator_id`, `price_score`, `technical_score`, `final_score`, `submitted`, `delivery_score`, `weighted_total`, `submitted_date`) VALUES
(3, 8, 10, 4, 100.00, 88.00, NULL, 1, 100.00, 95.80, '2026-05-02 22:03:02'),
(4, 8, 11, 4, 91.63, 79.01, NULL, 1, 82.89, 85.03, '2026-05-02 22:03:31'),
(5, 8, 10, 3, 100.00, 75.00, NULL, 1, 100.00, 91.25, '2026-05-02 22:04:30'),
(6, 8, 11, 3, 91.63, 79.00, NULL, 1, 82.89, 85.02, '2026-05-02 22:04:52'),
(7, 9, 13, 3, 100.00, 97.00, NULL, 1, 100.00, 98.95, '2026-05-07 18:46:42'),
(8, 9, 12, 3, 66.67, 78.00, NULL, 1, 83.33, 74.80, '2026-05-07 18:46:53'),
(9, 9, 13, 4, 100.00, 93.00, NULL, 1, 100.00, 97.55, '2026-05-07 18:47:19'),
(10, 9, 12, 4, 66.67, 86.00, NULL, 1, 83.33, 77.60, '2026-05-07 18:47:38');

-- --------------------------------------------------------

--
-- Table structure for table `final_scores`
--

CREATE TABLE `final_scores` (
  `final_score_id` int(11) NOT NULL,
  `bid_id` int(11) NOT NULL,
  `tender_id` int(11) NOT NULL,
  `avg_price_score` decimal(5,2) DEFAULT NULL,
  `avg_technical_score` decimal(5,2) DEFAULT NULL,
  `avg_delivery_score` decimal(5,2) DEFAULT NULL,
  `final_weighted_total` decimal(5,2) NOT NULL,
  `rank_position` int(11) DEFAULT NULL,
  `calculated_date` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `tenders`
--

CREATE TABLE `tenders` (
  `tender_id` int(11) NOT NULL,
  `reference_number` varchar(50) NOT NULL,
  `title` varchar(200) NOT NULL,
  `category` enum('Construction','Roads','Electrical','Plumbing','General Services') NOT NULL,
  `description` text NOT NULL,
  `estimated_value` decimal(15,2) NOT NULL,
  `submission_deadline` datetime NOT NULL,
  `status` enum('Draft','Open','Closed','Under_Evaluation','Evaluated','Awarded') DEFAULT 'Draft',
  `notice_file_path` varchar(500) NOT NULL,
  `created_by` int(11) NOT NULL,
  `created_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `closed_date` timestamp NULL DEFAULT NULL,
  `evaluated_date` timestamp NULL DEFAULT NULL,
  `awarded_date` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `tenders`
--

INSERT INTO `tenders` (`tender_id`, `reference_number`, `title`, `category`, `description`, `estimated_value`, `submission_deadline`, `status`, `notice_file_path`, `created_by`, `created_date`, `closed_date`, `evaluated_date`, `awarded_date`) VALUES
(8, 'MPW-2026-0001', 'Quthing Mountain Pass road construction', 'Roads', 'Carving a new road through the great mountains of Quthing', 224999999.98, '2027-05-02 22:56:00', 'Awarded', 'notices/1777755456145_1777420563356_C7-ADJ-11_End_Assessment_QP-ASR-001_Exam_Question_Paper.pdf', 1, '2026-05-02 20:57:36', '2026-05-02 22:01:14', NULL, '2026-05-02 23:06:46'),
(9, 'MPW-2026-0002', 'Afri Ski Road maintenance', 'Roads', 'Renewing road to Afri Ski resort', 50000000.00, '2026-10-07 20:41:00', 'Awarded', 'notices/1778179291788_C7-ADJ-11_End_Assessment_QP-ASR-001_Exam_Question_Paper.pdf', 1, '2026-05-07 18:41:32', '2026-05-07 18:46:17', NULL, '2026-05-07 18:49:32');

--
-- Triggers `tenders`
--
DELIMITER $$
CREATE TRIGGER `tender_status_change` AFTER UPDATE ON `tenders` FOR EACH ROW BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO audit_log (user_id, action, description)
        VALUES (NEW.created_by, 
                'TENDER_STATUS_CHANGE',
                CONCAT('Tender ', NEW.reference_number, ' changed from ', OLD.status, ' to ', NEW.status));
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `tender_evaluators`
--

CREATE TABLE `tender_evaluators` (
  `id` int(11) NOT NULL,
  `tender_id` int(11) NOT NULL,
  `evaluator_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `role` enum('SUPPLIER','PROCUREMENT_OFFICER','EVALUATION_COMMITTEE') NOT NULL,
  `registration_number` varchar(50) DEFAULT NULL,
  `company_name` varchar(150) DEFAULT NULL,
  `contact_number` varchar(20) DEFAULT NULL,
  `physical_address` text DEFAULT NULL,
  `account_locked` tinyint(1) DEFAULT 0,
  `failed_attempts` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `last_login` timestamp NULL DEFAULT NULL,
  `locked_until` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `email`, `password_hash`, `full_name`, `role`, `registration_number`, `company_name`, `contact_number`, `physical_address`, `account_locked`, `failed_attempts`, `created_at`, `last_login`, `locked_until`) VALUES
(1, 'peter.mokhosi', 'peter.mokhosi@ministry.gov.ls', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Peter Mokhosi', 'PROCUREMENT_OFFICER', NULL, NULL, NULL, NULL, 0, 0, '2026-04-18 20:29:57', '2026-05-07 21:41:26', NULL),
(2, 'lineo.makhalanyane', 'lineo.makhalanyane@ministry.gov.ls', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Lineo Makhalanyane', 'PROCUREMENT_OFFICER', NULL, NULL, NULL, NULL, 0, 0, '2026-04-18 20:29:57', NULL, NULL),
(3, 'thabo.masilo', 'thabo.masilo@ministry.gov.ls', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Thabo Masilo', 'EVALUATION_COMMITTEE', NULL, NULL, NULL, NULL, 0, 0, '2026-04-18 20:29:58', '2026-05-07 18:46:25', NULL),
(4, 'mamosa.lekhanya', 'mamosa.lekhanya@ministry.gov.ls', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Mamosa Lekhanya', 'EVALUATION_COMMITTEE', NULL, NULL, NULL, NULL, 0, 0, '2026-04-18 20:29:58', '2026-05-07 18:47:06', NULL),
(11, 'Karabo', 'tlotlisang16@gmail.com', '008c70392e3abfbd0fa47bbc2ed96aa99bd49e159727fcba0f2e6abeb3a9d601', 'Karabo Toka', 'SUPPLIER', 'REG-2026-0001', 'Karabo & Son Constructions', '+26664337790', 'Mafeteng', 0, 0, '2026-05-02 21:18:01', '2026-05-07 18:51:36', NULL),
(12, 'Thato', 'tlotlisangkhutlang@gmail.com', '008c70392e3abfbd0fa47bbc2ed96aa99bd49e159727fcba0f2e6abeb3a9d601', 'Thato Likalakala', 'SUPPLIER', 'REG-2026-0002', 'Thato & Co', '+26654326789', 'Qacha\'s Nek', 0, 0, '2026-05-02 21:54:41', '2026-05-07 21:42:16', NULL);

-- --------------------------------------------------------

--
-- Stand-in structure for view `vw_bids_with_suppliers`
-- (See below for the actual view)
--
CREATE TABLE `vw_bids_with_suppliers` (
`bid_id` int(11)
,`tender_id` int(11)
,`supplier_id` int(11)
,`company_name` varchar(150)
,`registration_number` varchar(50)
,`bid_amount` decimal(15,2)
,`technical_statement` varchar(600)
,`delivery_timeline` int(11)
,`submission_date` timestamp
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `vw_open_tenders`
-- (See below for the actual view)
--
CREATE TABLE `vw_open_tenders` (
`tender_id` int(11)
,`reference_number` varchar(50)
,`title` varchar(200)
,`category` enum('Construction','Roads','Electrical','Plumbing','General Services')
,`description` text
,`estimated_value` decimal(15,2)
,`submission_deadline` datetime
,`formatted_deadline` varchar(86)
);

-- --------------------------------------------------------

--
-- Structure for view `vw_bids_with_suppliers`
--
DROP TABLE IF EXISTS `vw_bids_with_suppliers`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vw_bids_with_suppliers`  AS SELECT `b`.`bid_id` AS `bid_id`, `b`.`tender_id` AS `tender_id`, `b`.`supplier_id` AS `supplier_id`, `u`.`company_name` AS `company_name`, `u`.`registration_number` AS `registration_number`, `b`.`bid_amount` AS `bid_amount`, `b`.`technical_statement` AS `technical_statement`, `b`.`delivery_timeline` AS `delivery_timeline`, `b`.`submission_date` AS `submission_date` FROM (`bids` `b` join `users` `u` on(`b`.`supplier_id` = `u`.`user_id`)) WHERE `b`.`is_active` = 1 ;

-- --------------------------------------------------------

--
-- Structure for view `vw_open_tenders`
--
DROP TABLE IF EXISTS `vw_open_tenders`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vw_open_tenders`  AS SELECT `tenders`.`tender_id` AS `tender_id`, `tenders`.`reference_number` AS `reference_number`, `tenders`.`title` AS `title`, `tenders`.`category` AS `category`, `tenders`.`description` AS `description`, `tenders`.`estimated_value` AS `estimated_value`, `tenders`.`submission_deadline` AS `submission_deadline`, date_format(`tenders`.`submission_deadline`,'%d %M %Y at %H:%i') AS `formatted_deadline` FROM `tenders` WHERE `tenders`.`status` = 'Open' AND `tenders`.`submission_deadline` > current_timestamp() ORDER BY `tenders`.`submission_deadline` ASC ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `audit_log`
--
ALTER TABLE `audit_log`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `idx_user` (`user_id`),
  ADD KEY `idx_action` (`action`),
  ADD KEY `idx_created_at` (`created_at`);

--
-- Indexes for table `awards`
--
ALTER TABLE `awards`
  ADD PRIMARY KEY (`award_id`),
  ADD UNIQUE KEY `tender_id` (`tender_id`),
  ADD KEY `winning_bid_id` (`winning_bid_id`),
  ADD KEY `winning_supplier_id` (`winning_supplier_id`),
  ADD KEY `idx_tender` (`tender_id`),
  ADD KEY `idx_award_date` (`award_date`);

--
-- Indexes for table `bids`
--
ALTER TABLE `bids`
  ADD PRIMARY KEY (`bid_id`),
  ADD UNIQUE KEY `unique_bid_per_tender` (`tender_id`,`supplier_id`),
  ADD KEY `idx_tender` (`tender_id`),
  ADD KEY `idx_supplier` (`supplier_id`),
  ADD KEY `idx_submission_date` (`submission_date`),
  ADD KEY `idx_bids_tender_active` (`tender_id`,`is_active`);

--
-- Indexes for table `email_notifications`
--
ALTER TABLE `email_notifications`
  ADD PRIMARY KEY (`notification_id`),
  ADD KEY `idx_tender` (`tender_id`),
  ADD KEY `idx_status` (`sent_status`),
  ADD KEY `idx_recipient` (`recipient_email`);

--
-- Indexes for table `evaluations`
--
ALTER TABLE `evaluations`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `bid_id` (`bid_id`,`evaluator_id`);

--
-- Indexes for table `final_scores`
--
ALTER TABLE `final_scores`
  ADD PRIMARY KEY (`final_score_id`),
  ADD UNIQUE KEY `bid_id` (`bid_id`),
  ADD KEY `idx_tender` (`tender_id`),
  ADD KEY `idx_rank` (`rank_position`),
  ADD KEY `idx_final_score` (`final_weighted_total`),
  ADD KEY `idx_final_scores_rank` (`tender_id`,`rank_position`);

--
-- Indexes for table `tenders`
--
ALTER TABLE `tenders`
  ADD PRIMARY KEY (`tender_id`),
  ADD UNIQUE KEY `reference_number` (`reference_number`),
  ADD KEY `created_by` (`created_by`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_reference` (`reference_number`),
  ADD KEY `idx_category` (`category`),
  ADD KEY `idx_deadline` (`submission_deadline`),
  ADD KEY `idx_tenders_status_deadline` (`status`,`submission_deadline`);

--
-- Indexes for table `tender_evaluators`
--
ALTER TABLE `tender_evaluators`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `tender_id` (`tender_id`,`evaluator_id`),
  ADD KEY `fk_te_user` (`evaluator_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `registration_number` (`registration_number`),
  ADD KEY `idx_role` (`role`),
  ADD KEY `idx_email` (`email`),
  ADD KEY `idx_registration_number` (`registration_number`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `audit_log`
--
ALTER TABLE `audit_log`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT for table `awards`
--
ALTER TABLE `awards`
  MODIFY `award_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `bids`
--
ALTER TABLE `bids`
  MODIFY `bid_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `email_notifications`
--
ALTER TABLE `email_notifications`
  MODIFY `notification_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `evaluations`
--
ALTER TABLE `evaluations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `final_scores`
--
ALTER TABLE `final_scores`
  MODIFY `final_score_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `tenders`
--
ALTER TABLE `tenders`
  MODIFY `tender_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `tender_evaluators`
--
ALTER TABLE `tender_evaluators`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `audit_log`
--
ALTER TABLE `audit_log`
  ADD CONSTRAINT `audit_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `awards`
--
ALTER TABLE `awards`
  ADD CONSTRAINT `awards_ibfk_1` FOREIGN KEY (`tender_id`) REFERENCES `tenders` (`tender_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `awards_ibfk_2` FOREIGN KEY (`winning_bid_id`) REFERENCES `bids` (`bid_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `awards_ibfk_3` FOREIGN KEY (`winning_supplier_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `bids`
--
ALTER TABLE `bids`
  ADD CONSTRAINT `bids_ibfk_1` FOREIGN KEY (`tender_id`) REFERENCES `tenders` (`tender_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `bids_ibfk_2` FOREIGN KEY (`supplier_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `email_notifications`
--
ALTER TABLE `email_notifications`
  ADD CONSTRAINT `email_notifications_ibfk_1` FOREIGN KEY (`tender_id`) REFERENCES `tenders` (`tender_id`) ON DELETE CASCADE;

--
-- Constraints for table `final_scores`
--
ALTER TABLE `final_scores`
  ADD CONSTRAINT `final_scores_ibfk_1` FOREIGN KEY (`bid_id`) REFERENCES `bids` (`bid_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `final_scores_ibfk_2` FOREIGN KEY (`tender_id`) REFERENCES `tenders` (`tender_id`) ON DELETE CASCADE;

--
-- Constraints for table `tenders`
--
ALTER TABLE `tenders`
  ADD CONSTRAINT `tenders_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `tender_evaluators`
--
ALTER TABLE `tender_evaluators`
  ADD CONSTRAINT `fk_te_tender` FOREIGN KEY (`tender_id`) REFERENCES `tenders` (`tender_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_te_user` FOREIGN KEY (`evaluator_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
