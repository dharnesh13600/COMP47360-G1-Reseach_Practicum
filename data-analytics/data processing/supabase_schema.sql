

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


CREATE SCHEMA IF NOT EXISTS "public";


ALTER SCHEMA "public" OWNER TO "pg_database_owner";


COMMENT ON SCHEMA "public" IS 'standard public schema';



CREATE OR REPLACE FUNCTION "public"."update_ml_predictions"("p_location_activity_score_id" "uuid", "p_cultural_activity_score" numeric, "p_crowd_score" numeric, "p_muse_score" numeric) RETURNS boolean
    LANGUAGE "plpgsql"
    AS $$
BEGIN
    UPDATE location_activity_scores 
    SET 
        cultural_activity_score = p_cultural_activity_score,
        crowd_score = p_crowd_score,
        muse_score = p_muse_score,
        ml_prediction_date = NOW(),
        updated_at = NOW()
    WHERE id = p_location_activity_score_id;
    RETURN FOUND;
END;
$$;


ALTER FUNCTION "public"."update_ml_predictions"("p_location_activity_score_id" "uuid", "p_cultural_activity_score" numeric, "p_crowd_score" numeric, "p_muse_score" numeric) OWNER TO "postgres";

SET default_tablespace = '';

SET default_table_access_method = "heap";


CREATE TABLE IF NOT EXISTS "public"."activities" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "name" character varying(50) NOT NULL
);


ALTER TABLE "public"."activities" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."event_locations" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "location_name" "text" NOT NULL,
    "latitude" numeric(10,7) NOT NULL,
    "longitude" numeric(10,7) NOT NULL,
    "nearest_taxi_zone_id" "uuid",
    "created_at" timestamp with time zone DEFAULT "now"(),
    "updated_at" timestamp with time zone DEFAULT "now"()
);


ALTER TABLE "public"."event_locations" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."location_activity_scores" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "event_id" integer NOT NULL,
    "location_id" "uuid" NOT NULL,
    "activity_id" "uuid" NOT NULL,
    "taxi_zone_id" "uuid" NOT NULL,
    "event_date" "date" NOT NULL,
    "event_time" time without time zone NOT NULL,
    "historical_taxi_zone_crowd_score" numeric(5,3),
    "historical_activity_score" numeric(5,2),
    "cultural_activity_score" numeric(5,2),
    "crowd_score" numeric(5,2),
    "muse_score" numeric(5,2),
    "ml_prediction_date" timestamp with time zone,
    "created_at" timestamp with time zone DEFAULT "now"(),
    "updated_at" timestamp with time zone DEFAULT "now"(),
    "estimated_crowd_number" integer
);


ALTER TABLE "public"."location_activity_scores" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."ml_prediction_logs" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "model_version" character varying(50) NOT NULL,
    "prediction_type" character varying(50) NOT NULL,
    "records_processed" integer NOT NULL,
    "records_updated" integer NOT NULL,
    "prediction_date" timestamp with time zone DEFAULT "now"(),
    "model_accuracy" numeric(5,4),
    "notes" "text"
);


ALTER TABLE "public"."ml_prediction_logs" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."request_analytics" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "activity_name" character varying(255) NOT NULL,
    "requested_hour" integer NOT NULL,
    "requested_day_of_week" integer NOT NULL,
    "request_count" integer DEFAULT 1,
    "last_requested" timestamp without time zone NOT NULL,
    "cache_hit" boolean DEFAULT false,
    "response_time_ms" bigint,
    "user_agent" "text"
);


ALTER TABLE "public"."request_analytics" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."taxi_zones" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "zone_name" character varying(100) NOT NULL,
    "latitude" numeric(10,7) NOT NULL,
    "longitude" numeric(10,7) NOT NULL,
    "created_at" timestamp with time zone DEFAULT "now"(),
    "updated_at" timestamp with time zone DEFAULT "now"()
);


ALTER TABLE "public"."taxi_zones" OWNER TO "postgres";


ALTER TABLE ONLY "public"."activities"
    ADD CONSTRAINT "activities_name_key" UNIQUE ("name");



ALTER TABLE ONLY "public"."activities"
    ADD CONSTRAINT "activities_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."event_locations"
    ADD CONSTRAINT "event_locations_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."location_activity_scores"
    ADD CONSTRAINT "location_activity_scores_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."ml_prediction_logs"
    ADD CONSTRAINT "ml_prediction_logs_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."request_analytics"
    ADD CONSTRAINT "request_analytics_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."taxi_zones"
    ADD CONSTRAINT "taxi_zones_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."taxi_zones"
    ADD CONSTRAINT "taxi_zones_zone_name_key" UNIQUE ("zone_name");



CREATE INDEX "idx_activities_name_lower" ON "public"."activities" USING "btree" ("lower"(("name")::"text"));



CREATE INDEX "idx_event_locations_coordinates" ON "public"."event_locations" USING "btree" ("latitude", "longitude");



CREATE INDEX "idx_event_locations_name" ON "public"."event_locations" USING "btree" ("location_name");



CREATE INDEX "idx_event_locations_taxi_zone" ON "public"."event_locations" USING "btree" ("nearest_taxi_zone_id");



CREATE INDEX "idx_location_activity_scores_activity" ON "public"."location_activity_scores" USING "btree" ("activity_id");



CREATE INDEX "idx_location_activity_scores_activity_hist_score" ON "public"."location_activity_scores" USING "btree" ("activity_id", "historical_activity_score" DESC);



CREATE INDEX "idx_location_activity_scores_composite" ON "public"."location_activity_scores" USING "btree" ("activity_id", "event_date", "event_time");



CREATE INDEX "idx_location_activity_scores_datetime" ON "public"."location_activity_scores" USING "btree" ("event_date", "event_time");



CREATE INDEX "idx_location_activity_scores_location" ON "public"."location_activity_scores" USING "btree" ("location_id");



CREATE INDEX "idx_location_activity_scores_location_id" ON "public"."location_activity_scores" USING "btree" ("location_id");



CREATE INDEX "idx_location_activity_scores_ml_ready" ON "public"."location_activity_scores" USING "btree" ("cultural_activity_score", "crowd_score") WHERE (("cultural_activity_score" IS NOT NULL) AND ("crowd_score" IS NOT NULL));



CREATE INDEX "idx_location_activity_scores_muse" ON "public"."location_activity_scores" USING "btree" ("muse_score") WHERE ("muse_score" IS NOT NULL);



CREATE INDEX "idx_ml_logs_date_type" ON "public"."ml_prediction_logs" USING "btree" ("prediction_date", "prediction_type");



CREATE INDEX "idx_taxi_zones_coordinates" ON "public"."taxi_zones" USING "btree" ("latitude", "longitude");



ALTER TABLE ONLY "public"."event_locations"
    ADD CONSTRAINT "event_locations_nearest_taxi_zone_id_fkey" FOREIGN KEY ("nearest_taxi_zone_id") REFERENCES "public"."taxi_zones"("id");



ALTER TABLE ONLY "public"."location_activity_scores"
    ADD CONSTRAINT "location_activity_scores_activity_id_fkey" FOREIGN KEY ("activity_id") REFERENCES "public"."activities"("id");



ALTER TABLE ONLY "public"."location_activity_scores"
    ADD CONSTRAINT "location_activity_scores_location_id_fkey" FOREIGN KEY ("location_id") REFERENCES "public"."event_locations"("id");



ALTER TABLE ONLY "public"."location_activity_scores"
    ADD CONSTRAINT "location_activity_scores_taxi_zone_id_fkey" FOREIGN KEY ("taxi_zone_id") REFERENCES "public"."taxi_zones"("id");



GRANT USAGE ON SCHEMA "public" TO "postgres";
GRANT USAGE ON SCHEMA "public" TO "anon";
GRANT USAGE ON SCHEMA "public" TO "authenticated";
GRANT USAGE ON SCHEMA "public" TO "service_role";



GRANT ALL ON FUNCTION "public"."update_ml_predictions"("p_location_activity_score_id" "uuid", "p_cultural_activity_score" numeric, "p_crowd_score" numeric, "p_muse_score" numeric) TO "anon";
GRANT ALL ON FUNCTION "public"."update_ml_predictions"("p_location_activity_score_id" "uuid", "p_cultural_activity_score" numeric, "p_crowd_score" numeric, "p_muse_score" numeric) TO "authenticated";
GRANT ALL ON FUNCTION "public"."update_ml_predictions"("p_location_activity_score_id" "uuid", "p_cultural_activity_score" numeric, "p_crowd_score" numeric, "p_muse_score" numeric) TO "service_role";



GRANT ALL ON TABLE "public"."activities" TO "anon";
GRANT ALL ON TABLE "public"."activities" TO "authenticated";
GRANT ALL ON TABLE "public"."activities" TO "service_role";



GRANT ALL ON TABLE "public"."event_locations" TO "anon";
GRANT ALL ON TABLE "public"."event_locations" TO "authenticated";
GRANT ALL ON TABLE "public"."event_locations" TO "service_role";



GRANT ALL ON TABLE "public"."location_activity_scores" TO "anon";
GRANT ALL ON TABLE "public"."location_activity_scores" TO "authenticated";
GRANT ALL ON TABLE "public"."location_activity_scores" TO "service_role";



GRANT ALL ON TABLE "public"."ml_prediction_logs" TO "anon";
GRANT ALL ON TABLE "public"."ml_prediction_logs" TO "authenticated";
GRANT ALL ON TABLE "public"."ml_prediction_logs" TO "service_role";



GRANT ALL ON TABLE "public"."request_analytics" TO "anon";
GRANT ALL ON TABLE "public"."request_analytics" TO "authenticated";
GRANT ALL ON TABLE "public"."request_analytics" TO "service_role";



GRANT ALL ON TABLE "public"."taxi_zones" TO "anon";
GRANT ALL ON TABLE "public"."taxi_zones" TO "authenticated";
GRANT ALL ON TABLE "public"."taxi_zones" TO "service_role";



ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "service_role";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "service_role";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "service_role";






RESET ALL;
