CREATE TABLE IF NOT EXISTS company(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(80) NOT NULL,
    industry   VARCHAR(80) NOT NULL,
    website    VARCHAR(80),
    date       DATE        NOT NULL
);

CREATE TABLE IF NOT EXISTS offer(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(80) NOT NULL,
    company_id  BIGINT      NOT NULL,
    status      ENUM('accepted', 'rejected', 'pending', 'interview') NOT NULL,
    date        DATE        NOT NULL,
    link        VARCHAR(255),
    description VARCHAR(255),
    notes       VARCHAR(255),
    salary      DECIMAL(10, 2),

    CONSTRAINT fk_company_id FOREIGN KEY (company_id)
        REFERENCES company(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS interview(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    offer_id BIGINT NOT NULL,
    date     DATE   NOT NULL,
    notes    VARCHAR(255),
    type     ENUM('telephone', 'technical', 'onsite', 'online') NOT NULL,
    outcome  ENUM('pending', 'positive', 'negative')            NOT NULL,

    CONSTRAINT fk_offer_id FOREIGN KEY (offer_id)
        REFERENCES offer(id)
        ON DELETE CASCADE
);