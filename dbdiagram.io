// Определение таблиц и связей для системы ClientExp

// Пользователи системы
Table users {
  id int [pk, increment]
  username varchar [not null]
  email varchar [not null, unique]
  password varchar [not null]
  role ENUM('ADMIN', 'MANAGER', 'USER') [not null]
  created_at timestamp [not null]
  updated_at timestamp
}

// Клиенты
Table clients {
  id int [pk, increment]
  name varchar [not null]
  email varchar [not null, unique]
  phone varchar
  age_group ENUM('UNDER_18', '18_25', '26_35', '36_45', '46_60', 'OVER_60')
  gender ENUM('MALE', 'FEMALE', 'OTHER')
  profession varchar
  region varchar
  location_preference ENUM('ONLINE', 'OFFLINE', 'HYBRID')
  usage_frequency ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'RARELY')
  source ENUM('ADVERTISING', 'RECOMMENDATION', 'SOCIAL_MEDIA', 'OTHER')
  social_network varchar
  usage_purpose ENUM('PERSONAL', 'BUSINESS', 'EDUCATION', 'OTHER')
  client_since date
  created_at timestamp [not null]
  updated_at timestamp
}

// Сегменты клиентов
Table segments {
  id int [pk, increment]
  name varchar [not null]
  description text
  criteria text
  created_at timestamp [not null]
  updated_at timestamp
}

// Связь клиентов и сегментов (многие ко многим)
Table client_segments {
  client_id int [ref: > clients.id]
  segment_id int [ref: > segments.id]
  indexes {
    (client_id, segment_id) [pk]
  }
}

// Опросы
Table surveys {
  id int [pk, increment]
  title varchar [not null]
  description text
  status ENUM('DRAFT', 'ACTIVE', 'COMPLETED', 'ARCHIVED') [not null]
  start_date date
  end_date date
  created_by int [ref: > users.id, not null]
  created_at timestamp [not null]
  updated_at timestamp
}

// Связь опросов и сегментов (многие ко многим)
Table survey_segments {
  survey_id int [ref: > surveys.id]
  segment_id int [ref: > segments.id]
  indexes {
    (survey_id, segment_id) [pk]
  }
}

// Категории вопросов
Table question_categories {
  id int [pk, increment]
  name varchar [not null]
  description text
  created_at timestamp [not null]
  updated_at timestamp
}

// Вопросы в опросах
Table questions {
  id int [pk, increment]
  survey_id int [ref: > surveys.id, not null]
  text text [not null]
  type ENUM('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TEXT', 'RATING') [not null]
  required boolean [not null]
  order_number int [not null]
  category varchar
  metric_type ENUM('NPS', 'CSAT', 'CES', 'OTHER')
  created_at timestamp [not null]
  updated_at timestamp
}

// Варианты ответов для вопросов
Table answer_options {
  id int [pk, increment]
  question_id int [ref: > questions.id, not null]
  text varchar [not null]
  order_number int [not null]
  created_at timestamp [not null]
  updated_at timestamp
}

// Ответы клиентов на опросы
Table client_answers {
  id int [pk, increment]
  client_id int [ref: > clients.id, not null]
  survey_id int [ref: > surveys.id, not null]
  question_id int [ref: > questions.id, not null]
  answer_option_id int [ref: > answer_options.id]
  text_answer text
  numeric_answer int
  created_at timestamp [not null]
}

// Обратная связь от клиентов
Table feedbacks {
  id int [pk, increment]
  client_id int [ref: > clients.id, not null]
  channel varchar [not null]
  content text [not null]
  sentiment ENUM('POSITIVE', 'NEUTRAL', 'NEGATIVE')
  resolved boolean [not null]
  created_at timestamp [not null]
  updated_at timestamp
}

// Взаимодействия с клиентами
Table client_interactions {
  id int [pk, increment]
  client_id int [ref: > clients.id, not null]
  type varchar [not null]
  channel varchar [not null]
  description text
  created_at timestamp [not null]
}

// Шаблоны рекомендаций
Table recommendation_templates {
  id int [pk, increment]
  title varchar [not null]
  content text [not null]
  category varchar
  created_by int [ref: > users.id, not null]
  created_at timestamp [not null]
  updated_at timestamp
}

// Рекомендации для клиентов
Table recommendations {
  id int [pk, increment]
  client_id int [ref: > clients.id, not null]
  template_id int [ref: > recommendation_templates.id]
  content text [not null]
  status ENUM('DRAFT', 'SENT', 'READ', 'COMPLETED')
  created_at timestamp [not null]
  updated_at timestamp
}

// Отчеты
Table reports {
  id int [pk, increment]
  title varchar [not null]
  type varchar [not null]
  parameters text
  created_by int [ref: > users.id, not null]
  file_path varchar
  format varchar
  created_at timestamp [not null]
} 