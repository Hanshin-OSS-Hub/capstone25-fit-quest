# RunningSession 저장 후 퀘스트 진행도 업데이트는 views.py에서만 처리합니다.
# signal에서 process_running_log를 호출하면 progress_value가 중복 증가하므로 비활성화합니다.
